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
import com.epam.dlab.exceptions.ResourceNotFoundException;
import com.epam.dlab.util.FileUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InfrastructureCallbackHandlerService {

	@Inject
	private ProvisioningServiceApplicationConfiguration configuration;

	public void saveObjectData(String uuid) {
		String handlerDirectory = configuration.getHandlerDirectory();
		String sourcePath = FileUtils.getIfExistsSimilar(uuid, handlerDirectory).orElseThrow(() ->
				new ResourceNotFoundException("File with name which contains " + uuid + " not found in directory " +
						handlerDirectory));
		FileUtils.copyFile(sourcePath, configuration.getHandlerDaoDirectory());
	}

}
