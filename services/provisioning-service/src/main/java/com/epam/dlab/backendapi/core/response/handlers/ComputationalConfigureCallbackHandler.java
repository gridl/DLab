/*
 * **************************************************************************
 *
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
 *
 * ***************************************************************************
 */

package com.epam.dlab.backendapi.core.response.handlers;

import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.service.InfrastructureCallbackHandlerService;
import com.epam.dlab.backendapi.service.SelfServiceHelper;
import com.epam.dlab.dto.base.computational.ComputationalBase;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ComputationalConfigureCallbackHandler extends ResourceCallbackHandler<ComputationalStatusDTO> {

	@JsonProperty
	private final ComputationalBase<?> dto;

	@JsonCreator
	public ComputationalConfigureCallbackHandler(@JacksonInject SelfServiceHelper selfServiceHelper,
												 @JacksonInject InfrastructureCallbackHandlerService
														 infrastructureCallbackHandlerService,
												 @JsonProperty("action") DockerAction action,
												 @JsonProperty("uuid") String uuid,
												 @JsonProperty("dto") ComputationalBase<?> dto) {
		super(selfServiceHelper, infrastructureCallbackHandlerService, dto.getCloudSettings().getIamUser(), uuid,
				action);
		this.dto = dto;
	}

	@Override
	protected String getCallbackURI() {
		return ApiCallbacks.COMPUTATIONAL + ApiCallbacks.STATUS_URI;
	}

	@Override
	protected ComputationalStatusDTO parseOutResponse(JsonNode resultNode, ComputationalStatusDTO baseStatus) {
		return baseStatus
				.withExploratoryName(dto.getExploratoryName())
				.withComputationalName(dto.getComputationalName())
				.withUptime(null);
	}
}
