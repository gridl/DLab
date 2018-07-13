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

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.service.EnvironmentService;
import com.epam.dlab.backendapi.swagger.SwaggerConfigurator;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("environment")
@Slf4j
@RolesAllowed("environment/*")
@Api(value = "Environment service", authorizations = @Authorization(SwaggerConfigurator.TOKEN_AUTH))
public class EnvironmentResource {

	private EnvironmentService environmentService;

	@Inject
	public EnvironmentResource(EnvironmentService environmentService) {
		this.environmentService = environmentService;
	}

	@GET
	@Path("user/active")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetches active users")
	@ApiResponses(value = @ApiResponse(code = 404, message = "Active users not found"))
	public Response getUsersWithActiveEnv(@ApiParam(hidden = true) @Auth UserInfo userInfo) {
		log.debug("User {} requested information about active environments", userInfo.getName());
		return Response.ok(environmentService.getActiveUsers()).build();
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("terminate")
	@ApiOperation(value = "Terminates user's environment including EDGE, notebooks, clusters")
	public Response terminateEnv(@ApiParam(hidden = true) @Auth UserInfo userInfo,
								 @ApiParam(value = "User's name", required = true) @NotEmpty String user) {
		log.info("User {} is terminating {} environment", userInfo.getName(), user);
		environmentService.terminateEnvironment(user);
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("stop")
	@ApiOperation(value = "Stops user's environment including EDGE, notebooks, Spark clusters")
	public Response stopEnv(@ApiParam(hidden = true) @Auth UserInfo userInfo,
							@ApiParam(value = "User's name", required = true) @NotEmpty String user) {
		log.info("User {} is stopping {} environment", userInfo.getName(), user);
		environmentService.stopEnvironment(user);
		return Response.ok().build();
	}
}
