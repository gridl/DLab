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

package com.epam.dlab.backendapi.core.response.handlers;

import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.service.InfrastructureCallbackHandlerService;
import com.epam.dlab.backendapi.service.SelfServiceHelper;
import com.epam.dlab.dto.UserInstanceStatus;
import com.epam.dlab.dto.exploratory.LibInstallDTO;
import com.epam.dlab.dto.exploratory.LibInstallStatusDTO;
import com.epam.dlab.dto.exploratory.LibraryInstallDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Handler of docker response for the request for libraries installation.
 */
@Slf4j
public class LibInstallCallbackHandler extends ResourceCallbackHandler<LibInstallStatusDTO> {

	/**
	 * Name of node in response "Libs".
	 */
	private static final String LIBS = "Libs";

	/**
	 * Full name of node in response "Libs".
	 */
	private static final String LIBS_ABSOLUTE_PATH = RESPONSE_NODE + "." + RESULT_NODE + "." + LIBS;

	/**
	 * Exploratory DTO.
	 */
	@JsonProperty
	private final LibraryInstallDTO dto;

	/**
	 * Instantiate handler for process of docker response for libraries installation.
	 *
	 * @param selfServiceHelper pointer for Self Service helper.
	 * @param action            docker action.
	 * @param uuid              request UID.
	 * @param dto               contains libraries to instal
	 */
	@JsonCreator
	public LibInstallCallbackHandler(@JacksonInject SelfServiceHelper selfServiceHelper,
									 @JacksonInject InfrastructureCallbackHandlerService
											 infrastructureCallbackHandlerService,
									 @JsonProperty("action") DockerAction action,
									 @JsonProperty("uuid") String uuid, @JsonProperty("user") String user,
									 @JsonProperty("dto") LibraryInstallDTO dto) {
		super(selfServiceHelper, infrastructureCallbackHandlerService, user, uuid, action);
		this.dto = dto;
	}

	@Override
	protected String getCallbackURI() {
		return ApiCallbacks.LIB_STATUS_URI;
	}

	@Override
	protected LibInstallStatusDTO parseOutResponse(JsonNode resultNode, LibInstallStatusDTO status) {

		if (UserInstanceStatus.FAILED == UserInstanceStatus.of(status.getStatus())) {
			for (LibInstallDTO lib : dto.getLibs()) {
				lib.withStatus(status.getStatus()).withErrorMessage(status.getErrorMessage());
			}
			return status.withLibs(dto.getLibs());
		}
		if (resultNode == null) {
			throw new DlabException("Can't handle response result node is null");
		}

		JsonNode nodeLibs = resultNode.get(LIBS);
		if (nodeLibs == null) {
			throw new DlabException("Can't handle response without property " + LIBS_ABSOLUTE_PATH);
		}
		try {
			status.withLibs(mapper.readValue(nodeLibs.toString(), new TypeReference<List<LibInstallDTO>>() {
			}));
		} catch (IOException e) {
			log.warn("Can't parse field {} for UUID {} in JSON", LIBS_ABSOLUTE_PATH, getUUID(), e);
		}

		return status;
	}

	@Override
	protected LibInstallStatusDTO getBaseStatusDTO(UserInstanceStatus status) {
		return super.getBaseStatusDTO(status)
				.withExploratoryName(dto.getExploratoryName())
				.withUptime(Date.from(Instant.now()))
				.withComputationalName(dto.getComputationalName());
	}
}
