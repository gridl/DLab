/*
 * Copyright (c) 2018, EPAM SYSTEMS INC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.dlab.backendapi.service;

import com.epam.dlab.backendapi.core.Directories;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.backendapi.core.commands.RunDockerCommand;
import com.epam.dlab.backendapi.core.response.handlers.ReuploadKeyCallbackHandler;
import com.epam.dlab.dto.reuploadkey.ReuploadKeyCallbackDTO;
import com.epam.dlab.dto.reuploadkey.ReuploadKeyDTO;
import com.epam.dlab.model.ResourceData;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReuploadKeyService extends DockerService implements DockerCommands {

	private static final String REUPLOAD_KEY_ACTION = "reupload_key";

	public void reuploadKeyAction(String userName, ReuploadKeyDTO dto, DockerAction action) {
		log.debug("{} for edge user {}", action, dto.getEdgeUserName());

		long count = dto.getResources()
				.stream()
				.map(resourceData -> buildCallbackDTO(resourceData, getUuid(), dto))
				.peek(callbackDto -> startCallbackListener(userName, callbackDto))
				.peek(callbackDto ->
						runDockerCmd(userName, callbackDto.getId(), buildRunDockerCommand(callbackDto, action),
								buildDockerCommandDTO(callbackDto)))
				.count();
		log.debug("Executed {} Docker commands", count);
	}

	private String getUuid() {
		return DockerCommands.generateUUID();
	}

	private void runDockerCmd(String userName, String uuid, RunDockerCommand runDockerCommand,
							  ReuploadKeyCallbackDTO callbackDto) {
		try {
			final String command = commandBuilder.buildCommand(runDockerCommand, callbackDto);
			log.trace("Docker command: {}", command);
			commandExecutor.executeAsync(userName, uuid, command);
		} catch (Exception e) {
			log.error("Exception occured during reuploading key: {} for command {}", e.getLocalizedMessage(),
					runDockerCommand.toCMD());
		}
	}

	private void startCallbackListener(String userName, ReuploadKeyCallbackDTO dto) {
		folderListenerExecutor.start(configuration.getKeyLoaderDirectory(),
				configuration.getKeyLoaderPollTimeout(),
				new ReuploadKeyCallbackHandler(selfServiceHelper, ApiCallbacks.REUPLOAD_KEY_URI, userName, dto));
	}

	@Override
	public String getResourceType() {
		return Directories.EDGE_LOG_DIRECTORY;
	}

	private RunDockerCommand buildRunDockerCommand(ReuploadKeyCallbackDTO callbackDto, DockerAction action) {
		return new RunDockerCommand()
				.withInteractive()
				.withName(getContainerName(callbackDto))
				.withVolumeForRootKeys(configuration.getKeyDirectory())
				.withVolumeForResponse(configuration.getKeyLoaderDirectory())
				.withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
				.withResource(callbackDto.getResource().getResourceType().toString())
				.withRequestId(callbackDto.getId())
				.withConfKeyName(configuration.getAdminKey())
				.withImage(configuration.getEdgeImage())
				.withAction(action);
	}

	private ReuploadKeyCallbackDTO buildCallbackDTO(ResourceData resource, String uuid, ReuploadKeyDTO dto) {
		return new ReuploadKeyCallbackDTO()
				.withId(uuid)
				.withEdgeUserName(dto.getEdgeUserName())
				.withServiceBaseName(dto.getServiceBaseName())
				.withConfOsFamily(dto.getConfOsFamily())
				.withResourceId(resource.getResourceId())
				.withResource(resource);
	}

	private ReuploadKeyCallbackDTO buildDockerCommandDTO(ReuploadKeyCallbackDTO dto) {
		return new ReuploadKeyCallbackDTO()
				.withEdgeUserName(dto.getEdgeUserName())
				.withServiceBaseName(dto.getServiceBaseName())
				.withConfOsFamily(dto.getConfOsFamily())
				.withResourceId(dto.getResourceId());
	}

	private String getContainerName(ReuploadKeyCallbackDTO callbackDto) {
		return nameContainer(callbackDto.getEdgeUserName(), REUPLOAD_KEY_ACTION,
				callbackDto.getResource().getResourceType().toString(),
				callbackDto.getResource().getResourceId());
	}

}
