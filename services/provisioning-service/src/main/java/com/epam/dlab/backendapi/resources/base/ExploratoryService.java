/*
 * Copyright (c) 2017, EPAM SYSTEMS INC
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

package com.epam.dlab.backendapi.resources.base;

import com.epam.dlab.backendapi.core.Directories;
import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.backendapi.core.commands.RunDockerCommand;
import com.epam.dlab.backendapi.core.response.handlers.ExploratoryCallbackHandler;
import com.epam.dlab.backendapi.service.DockerService;
import com.epam.dlab.dto.exploratory.ExploratoryBaseDTO;
import com.epam.dlab.process.model.ProcessType;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
public class ExploratoryService extends DockerService implements DockerCommands {

	private static Map<DockerAction, ProcessType> processTypeMap = new EnumMap<>(DockerAction.class);

	static {
		processTypeMap.put(DockerAction.CREATE, ProcessType.EXPLORATORY_CREATE);
		processTypeMap.put(DockerAction.START, ProcessType.EXPLORATORY_START);
		processTypeMap.put(DockerAction.STOP, ProcessType.EXPLORATORY_STOP);
		processTypeMap.put(DockerAction.TERMINATE, ProcessType.EXPLORATORY_TERMINATE);
	}

    public String action(String username, ExploratoryBaseDTO<?> dto, DockerAction action) throws JsonProcessingException {
        log.debug("{} exploratory environment", action);
        String uuid = DockerCommands.generateUUID();
        folderListenerExecutor.start(configuration.getImagesDirectory(),
                configuration.getResourceStatusPollTimeout(),
                getFileHandlerCallback(action, uuid, dto));

        RunDockerCommand runDockerCommand = new RunDockerCommand()
                .withInteractive()
                .withName(nameContainer(dto.getEdgeUserName(), action, dto.getExploratoryName()))
                .withVolumeForRootKeys(configuration.getKeyDirectory())
                .withVolumeForResponse(configuration.getImagesDirectory())
                .withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
                .withResource(getResourceType())
                .withRequestId(uuid)
                .withConfKeyName(configuration.getAdminKey())
                .withImage(dto.getNotebookImage())
                .withAction(action);

		final String processDescription = String.format("Exploratory_name: %s", dto.getExploratoryName());
		commandExecutor.startAsync(username, uuid, processTypeMap.get(action), processDescription,
				commandBuilder.buildCommand(runDockerCommand, dto));
        return uuid;
    }

    public String getResourceType() {
        return Directories.NOTEBOOK_LOG_DIRECTORY;
    }

    private FileHandlerCallback getFileHandlerCallback(DockerAction action, String uuid, ExploratoryBaseDTO<?> dto) {
        return new ExploratoryCallbackHandler(selfService, action, uuid, dto.getCloudSettings().getIamUser(),
                dto.getExploratoryName());
    }

    private String nameContainer(String user, DockerAction action, String name) {
        return nameContainer(user, action.toString(), "exploratory", name);
    }
}
