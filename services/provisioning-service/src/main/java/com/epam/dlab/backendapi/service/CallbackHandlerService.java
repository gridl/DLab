/*
 *
 *  * Copyright (c) 2018, EPAM SYSTEMS INC
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package com.epam.dlab.backendapi.service;

import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.core.response.handlers.*;
import com.epam.dlab.dto.DtoType;
import com.epam.dlab.dto.aws.computational.AwsComputationalTerminateDTO;
import com.epam.dlab.dto.aws.computational.ComputationalCreateAws;
import com.epam.dlab.dto.backup.EnvBackupDTO;
import com.epam.dlab.dto.computational.ComputationalStartDTO;
import com.epam.dlab.dto.computational.ComputationalStopDTO;
import com.epam.dlab.dto.computational.ComputationalTerminateDTO;
import com.epam.dlab.dto.exploratory.ExploratoryImageDTO;
import com.epam.dlab.dto.exploratory.LibraryInstallDTO;
import com.epam.dlab.dto.gcp.computational.ComputationalCreateGcp;
import com.epam.dlab.dto.gcp.computational.GcpComputationalTerminateDTO;
import com.epam.dlab.dto.handlers.*;
import com.epam.dlab.dto.handlers.helper.HandlerHelper;
import com.epam.dlab.dto.reuploadkey.ReuploadKeyCallbackDTO;
import com.epam.dlab.exceptions.DlabException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class CallbackHandlerService extends DockerService {

	@Inject
	private ComputationalConfigure computationalConfigure;
	private ObjectMapper mapper = new ObjectMapper();

	public void restartHandlers() {
		List<String> fileNames =
				HandlerHelper.getInstance().getHandlerInfoFileNames(configuration.getHandlerDirectory());
		log.debug("Found {} callback handlers", fileNames.size());
		if (!fileNames.isEmpty()) {
			List<FileHandlerCallback> handlerInfo = convertedHandlerInfo(fileNames);
			log.debug("Rebuilt {} handlers", handlerInfo.size());
			restartAll(handlerInfo);
		}
	}

	@SuppressWarnings("unchecked")
	private List<FileHandlerCallback> convertedHandlerInfo(List<String> fileNames) {
		List<FileHandlerCallback> handlers = new ArrayList<>();
		for (String fileName : fileNames) {
			JsonNode jsonNode;
			try {
				jsonNode = mapper.readTree(new File(fileName));
			} catch (IOException e) {
				log.error("An exception occured when accessed file {}: {}", fileName, e);
				throw new DlabException("Problems occured with accessing file " + fileName);
			}
			CallBackHandlerType handlerType = CallBackHandlerType.of(jsonNode.get("handler_type").textValue());
			Class<? extends BaseCallbackHandlerDTO> handlerClass =
					HandlerHelper.getInstance().getHandlerDtoClass(handlerType).orElseThrow(() ->
							new DlabException("Class of callback handler wasn't recognized"));
			JsonNode dtoNode = jsonNode.get("data_transfer_object");
			DtoType type = DtoType.of(dtoNode.get("dto_type").textValue());
			Class dtoClass = HandlerHelper.getInstance().getDtoClass(type).orElseThrow(() ->
					new DlabException("Class of embedded DTO object in callback handler wasn't recognized"));
			FileHandlerCallback fileHandlerCallback = getRestoredHandler(handlerType,
					getHandler(handlerClass, jsonNode), dtoClass, getDtoObject(dtoClass, dtoNode));
			handlers.add(fileHandlerCallback);
		}
		return handlers;
	}

	@SuppressWarnings("unchecked")
	private <T> FileHandlerCallback getRestoredHandler(CallBackHandlerType handlerType, BaseCallbackHandlerDTO
			handlerData, Class dtoClass, T dtoData) {
		String handlerDirectory = configuration.getHandlerDirectory();
		if (handlerType == CallBackHandlerType.BACKUP_HANDLER) {
			BackupCallbackHandlerDTO dto = (BackupCallbackHandlerDTO) handlerData;
			return new BackupCallbackHandler(selfService, dto.getCallbackUri(), dto.getUser(), handlerDirectory,
					(EnvBackupDTO) dtoData);
		} else if (handlerType == CallBackHandlerType.COMPUTATIONAL_HANDLER) {
			ComputationalCallbackHandler computationalCallbackHandler =
					new ComputationalCallbackHandler(computationalConfigure, selfService,
							DockerAction.of(handlerData.getDockerAction()), handlerData.getUuid(), handlerDirectory,
							new ComputationalCreateAws());
			if (dtoClass == ComputationalCreateAws.class) {
				computationalCallbackHandler.setDto((ComputationalCreateAws) dtoData);
			} else if (dtoClass == AwsComputationalTerminateDTO.class) {
				computationalCallbackHandler.setDto((AwsComputationalTerminateDTO) dtoData);
			} else if (dtoClass == ComputationalCreateGcp.class) {
				computationalCallbackHandler.setDto((ComputationalCreateGcp) dtoData);
			} else if (dtoClass == GcpComputationalTerminateDTO.class) {
				computationalCallbackHandler.setDto((GcpComputationalTerminateDTO) dtoData);
			} else if (dtoClass == ComputationalStartDTO.class) {
				computationalCallbackHandler.setDto((ComputationalStartDTO) dtoData);
			} else if (dtoClass == ComputationalStopDTO.class) {
				computationalCallbackHandler.setDto((ComputationalStopDTO) dtoData);
			} else if (dtoClass == ComputationalTerminateDTO.class) {
				computationalCallbackHandler.setDto((ComputationalTerminateDTO) dtoData);
			}
			return computationalCallbackHandler;
		} else if (handlerType == CallBackHandlerType.EDGE_HANDLER) {
			EdgeCallbackHandlerDTO dto = (EdgeCallbackHandlerDTO) handlerData;
			try {
				return new EdgeCallbackHandler(selfService, DockerAction.of(dto.getDockerAction()),
						dto.getUuid(), dto.getUser(), dto.getCallbackUri(), Class.forName(dto.getResponseClass()),
						Class.forName(dto.getEnclosingClass()), handlerDirectory);
			} catch (ClassNotFoundException e) {
				log.error("An exception occured while restoring callback handler data: {}", e);
				throw new DlabException("Couldn't restore callback handler with data of type " + handlerType);
			}
		} else if (handlerType == CallBackHandlerType.EXPLORATORY_HANDLER) {
			ExploratoryCallbackHandlerDTO dto = (ExploratoryCallbackHandlerDTO) handlerData;
			return new ExploratoryCallbackHandler(selfService, DockerAction.of(dto.getDockerAction()),
					dto.getUuid(), dto.getUser(), dto.getExploratoryName(), handlerDirectory);
		} else if (handlerType == CallBackHandlerType.EXPLORATORY_GIT_CREDS_HANDLER) {
			ExploratoryGitCredsCallbackHandlerDTO dto = (ExploratoryGitCredsCallbackHandlerDTO) handlerData;
			return new ExploratoryGitCredsCallbackHandler(selfService, DockerAction.of(dto.getDockerAction()),
					dto.getUuid(), dto.getUser(), dto.getExploratoryName(), handlerDirectory);
		} else if (handlerType == CallBackHandlerType.IMAGE_CREATE_HANDLER) {
			return new ImageCreateCallbackHandler(selfService, handlerData.getUuid(),
					DockerAction.of(handlerData.getDockerAction()), handlerDirectory, (ExploratoryImageDTO) dtoData);
		} else if (handlerType == CallBackHandlerType.LIB_INSTALL_HANDLER) {
			return new LibInstallCallbackHandler(selfService, DockerAction.of(handlerData.getDockerAction()),
					handlerData.getUuid(), handlerData.getUser(), handlerDirectory, (LibraryInstallDTO) dtoData);
		} else if (handlerType == CallBackHandlerType.LIB_LIST_HANDLER) {
			LibListCallbackHandlerDTO dto = (LibListCallbackHandlerDTO) handlerData;
			return new LibListCallbackHandler(selfService, DockerAction.of(dto.getDockerAction()),
					dto.getUuid(), dto.getUser(), dto.getImageName(), handlerDirectory);
		} else if (handlerType == CallBackHandlerType.RESOURCES_STATUS_HANDLER) {
			return new ResourcesStatusCallbackHandler(selfService, DockerAction.of(handlerData.getDockerAction()),
					handlerData.getUuid(), handlerData.getUser(), handlerDirectory);
		} else if (handlerType == CallBackHandlerType.REUPLOAD_KEY_HANDLER) {
			ReuploadKeyCallbackHandlerDTO dto = (ReuploadKeyCallbackHandlerDTO) handlerData;
			return new ReuploadKeyCallbackHandler(selfService, dto.getCallbackUri(), dto.getUser(),
					handlerDirectory, (ReuploadKeyCallbackDTO) dtoData);
		} else throw new DlabException("Incompatible callback handler type");
	}

	@SuppressWarnings("unchecked")
	private <T> T getDtoObject(Class<T> dtoClass, JsonNode dtoNode) {
		try {
			return mapper.readValue(dtoNode.get("transfer_data").toString(), dtoClass);
		} catch (IOException e) {
			log.error("An exception occured with trying to parse JSON into object of {}: {}", dtoClass, e);
			throw new DlabException("Couldn't parse JSON into object " + dtoClass);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends BaseCallbackHandlerDTO> T getHandler(Class<T> handlerDtoClass, JsonNode json) {
		try {
			return mapper.readValue(json.toString(), handlerDtoClass);
		} catch (IOException e) {
			log.error("An exception occured with trying to parse JSON into object of {}: {}", handlerDtoClass, e);
			throw new DlabException("Couldn't parse JSON into object " + handlerDtoClass);
		}
	}

	private void restartAll(List<FileHandlerCallback> handlerInfo) {
		log.debug("Restarting all callback handlers...");
		handlerInfo.forEach(handler -> folderListenerExecutor
				.start(configuration.getKeyLoaderDirectory(), configuration.getKeyLoaderPollTimeout(), handler));
	}
}
