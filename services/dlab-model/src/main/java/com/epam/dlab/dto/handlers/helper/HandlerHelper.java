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
package com.epam.dlab.dto.handlers.helper;

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
import com.epam.dlab.dto.reuploadkey.ReuploadKeyCallbackDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.exceptions.ResourceNotFoundException;
import com.epam.dlab.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class HandlerHelper {

	private Map<CallBackHandlerType, Class<? extends BaseCallbackHandlerDTO>> handlerDtoClasses;
	private Map<DtoType, Class> dtoClasses;
	private static HandlerHelper instance;

	private HandlerHelper() {
	}

	public static synchronized HandlerHelper getInstance() {
		if (instance == null) {
			instance = new HandlerHelper();
			instance.initializeMaps();
		}
		return instance;
	}

	private void initializeMaps() {
		initializeHandlerDtoClasses();
		initializeDtoClasses();
	}

	private void initializeHandlerDtoClasses() {
		handlerDtoClasses = new EnumMap<>(CallBackHandlerType.class);
		handlerDtoClasses.put(CallBackHandlerType.BACKUP_HANDLER, BackupCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.COMPUTATIONAL_HANDLER, BaseCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.EDGE_HANDLER, EdgeCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.EXPLORATORY_HANDLER, ExploratoryCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.EXPLORATORY_GIT_CREDS_HANDLER, ExploratoryGitCredsCallbackHandlerDTO
				.class);
		handlerDtoClasses.put(CallBackHandlerType.IMAGE_CREATE_HANDLER, BaseCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.LIB_INSTALL_HANDLER, BaseCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.LIB_LIST_HANDLER, LibListCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.RESOURCES_STATUS_HANDLER, BaseCallbackHandlerDTO.class);
		handlerDtoClasses.put(CallBackHandlerType.REUPLOAD_KEY_HANDLER, ReuploadKeyCallbackHandlerDTO.class);
	}

	private void initializeDtoClasses() {
		dtoClasses = new EnumMap<>(DtoType.class);
		dtoClasses.put(DtoType.COMPUTATIONAL_CREATE_AWS, ComputationalCreateAws.class);
		dtoClasses.put(DtoType.AWS_COMPUTATIONAL_TERMINATE, AwsComputationalTerminateDTO.class);
		dtoClasses.put(DtoType.COMPUTATIONAL_CREATE_GCP, ComputationalCreateGcp.class);
		dtoClasses.put(DtoType.GCP_COMPUTATIONAL_TERMINATE, GcpComputationalTerminateDTO.class);
		dtoClasses.put(DtoType.COMPUTATIONAL_START, ComputationalStartDTO.class);
		dtoClasses.put(DtoType.COMPUTATIONAL_STOP, ComputationalStopDTO.class);
		dtoClasses.put(DtoType.COMPUTATIONAL_TERMINATE, ComputationalTerminateDTO.class);
		dtoClasses.put(DtoType.EXPLORATORY_IMAGE, ExploratoryImageDTO.class);
		dtoClasses.put(DtoType.LIBRARY_INSTALL, LibraryInstallDTO.class);
		dtoClasses.put(DtoType.ENV_BACKUP, EnvBackupDTO.class);
		dtoClasses.put(DtoType.REUPLOAD_KEY_CALLBACK, ReuploadKeyCallbackDTO.class);
	}

	public Optional<Class<? extends BaseCallbackHandlerDTO>> getHandlerDtoClass(CallBackHandlerType type) {
		return Optional.ofNullable(handlerDtoClasses.get(type));
	}

	public Optional<Class> getDtoClass(DtoType type) {
		return Optional.ofNullable(dtoClasses.get(type));
	}

	public List<String> getHandlerInfoFileNames(String handlerDirectory) {
		log.debug("Searching callback handlers in directory {}...", handlerDirectory);
		try (Stream<Path> paths = Files.walk(Paths.get(handlerDirectory))) {
			return paths
					.filter(Files::isRegularFile)
					.map(Path::toString)
					.collect(Collectors.toList());
		} catch (IOException e) {
			log.error("An exception occured when accessed directory {}: {}", handlerDirectory, e);
			throw new DlabException("Problems occured with accessing directory " + handlerDirectory);
		}
	}

	public void deleteHandlerFile(String requestId, String handlerDirectory) {
		String fileToRemove = getHandlerInfoFileNames(handlerDirectory).stream()
				.filter(name -> name.contains(requestId)).findAny().orElseThrow(() ->
						new ResourceNotFoundException("File with callback handler data not found."));
		FileUtils.deleteFile(fileToRemove);
	}

}
