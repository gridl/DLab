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

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.resources.dto.HealthStatusPageDTO;
import com.epam.dlab.backendapi.resources.dto.InfrastructureInfo;
import com.epam.dlab.backendapi.roles.UserRoles;
import com.epam.dlab.backendapi.service.InfrastructureInfoService;
import com.epam.dlab.backendapi.swagger.SwaggerConfigurator;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Provides the REST API for the basic information about infrastructure.
 */
@Path("/infrastructure")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Infrastructure info service", authorizations = {@Authorization(SwaggerConfigurator.BASIC_AUTH),
		@Authorization(SwaggerConfigurator.TOKEN_AUTH)})
@Slf4j
public class InfrastructureInfoResource {

	private InfrastructureInfoService infrastructureInfoService;

	@Inject
	public InfrastructureInfoResource(InfrastructureInfoService infrastructureInfoService) {
		this.infrastructureInfoService = infrastructureInfoService;
	}

	/**
	 * Return status of self-service.
	 */
	@GET
	@ApiOperation(value = "Returns status of self-service")
	public Response status() {
		return Response.status(Response.Status.OK).build();
	}

	/**
	 * Returns the status of infrastructure: edge.
	 *
	 * @param userInfo user info.
	 */
	@GET
	@Path("/status")
	@ApiOperation(value = "Returns EDGE's status")
	public HealthStatusPageDTO status(@Auth UserInfo userInfo,
									  @ApiParam(value = "Full version of report required", defaultValue = "0")
									  @QueryParam("full") @DefaultValue("0") int fullReport) {
		return infrastructureInfoService
				.getHeathStatus(userInfo.getName(), fullReport != 0, UserRoles.isAdmin(userInfo));
	}

	/**
	 * Returns the list of the provisioned user resources.
	 *
	 * @param userInfo user info.
	 */
	@GET
	@Path("/info")
	@ApiOperation(value = "Returns list of user's resources")
	public InfrastructureInfo getUserResources(@Auth UserInfo userInfo) {
		return infrastructureInfoService.getUserResources(userInfo.getName());

	}
}
