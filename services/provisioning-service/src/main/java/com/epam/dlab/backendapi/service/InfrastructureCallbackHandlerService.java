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

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.dto.PersistentStatusDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Singleton
public class InfrastructureCallbackHandlerService {

	@Inject
	private ProvisioningServiceApplicationConfiguration configuration;
	@Inject
	private ObjectMapper mapper;

	public void save(PersistentStatusDto object) {
		final String fileName = fileName(object);
		final String absolutePath = getAbsolutePath(fileName);
		saveToFile(object, fileName, absolutePath);
	}

	private void saveToFile(PersistentStatusDto object, String fileName, String absolutePath) {
		try {
			log.trace("Persisting status dto object to file {}", absolutePath);
			Files.write(Paths.get(absolutePath), mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsBytes(object), StandardOpenOption.CREATE);
		} catch (Exception e) {
			log.warn("Can not save status dto object {} due to {}", fileName, e.getMessage());
		}
	}

	private String fileName(PersistentStatusDto object) {
		return object.getDto().getClass().getSimpleName() + "_" + object.getUuid() + ".json";
	}

	private String getAbsolutePath(String fileName) {
		return configuration.getStatusDtoDirectory() + File.separator + fileName;
	}

}
