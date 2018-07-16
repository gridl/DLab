/***************************************************************************

 Copyright (c) 2016, EPAM SYSTEMS INC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 ****************************************************************************/

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.rest.UserSessionDurationAuthorizer;
import com.epam.dlab.backendapi.resources.dto.ExploratoryActionFormDTO;
import com.epam.dlab.backendapi.resources.dto.ExploratoryCreateFormDTO;
import com.epam.dlab.backendapi.roles.RoleType;
import com.epam.dlab.backendapi.roles.UserRoles;
import com.epam.dlab.backendapi.service.ExploratoryService;
import com.epam.dlab.backendapi.swagger.SwaggerConfigurator;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.model.exloratory.Exploratory;
import com.epam.dlab.rest.contracts.ExploratoryAPI;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Provides the REST API for the exploratory.
 */
@Path("/infrastructure_provision/exploratory_environment")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Notebook service", authorizations = @Authorization(SwaggerConfigurator.TOKEN_AUTH))
@Slf4j
public class ExploratoryResource implements ExploratoryAPI {

	private ExploratoryService exploratoryService;

	@Inject
	public ExploratoryResource(ExploratoryService exploratoryService) {
		this.exploratoryService = exploratoryService;
	}

	/**
	 * Creates the exploratory environment for user.
	 *
	 * @param userInfo user info.
	 * @param formDTO  description for the exploratory environment.
	 * @return {@link Response.Status#OK} request for provisioning service has been accepted.<br>
	 * {@link Response.Status#FOUND} request for provisioning service has been duplicated.
	 */
	@PUT
	@RolesAllowed(UserSessionDurationAuthorizer.SHORT_USER_SESSION_DURATION)
	@ApiOperation(value = "Creates notebook")
	@ApiResponses(value = {@ApiResponse(code = 302, message = "Notebook with current parameters already exists"),
			@ApiResponse(code = 200, message = "Notebook created successfully")})
	public Response create(@ApiParam(hidden = true) @Auth UserInfo userInfo,
						   @ApiParam(value = "Notebook create form DTO", required = true)
						   @Valid @NotNull ExploratoryCreateFormDTO formDTO) {
		log.debug("Creating exploratory environment {} with name {} for user {}",
				formDTO.getImage(), formDTO.getName(), userInfo.getName());
		if (!UserRoles.checkAccess(userInfo, RoleType.EXPLORATORY, formDTO.getImage())) {
			log.warn("Unauthorized attempt to create a {} by user {}", formDTO.getImage(), userInfo.getName());
			throw new DlabException("You do not have the privileges to create a " + formDTO.getTemplateName());
		}
		String uuid = exploratoryService.create(userInfo, getExploratory(formDTO));
		return Response.ok(uuid).build();

	}


	/**
	 * Starts exploratory environment for user.
	 *
	 * @param userInfo user info.
	 * @param formDTO  description of exploratory action.
	 * @return Invocation response as JSON string.
	 */
	@POST
	@RolesAllowed(UserSessionDurationAuthorizer.SHORT_USER_SESSION_DURATION)
	@ApiOperation(value = "Starts notebook by name")
	public String start(@ApiParam(hidden = true) @Auth UserInfo userInfo,
						@ApiParam(value = "Notebook action form DTO", required = true)
						@Valid @NotNull ExploratoryActionFormDTO formDTO) {
		log.debug("Starting exploratory environment {} for user {}", formDTO.getNotebookInstanceName(),
				userInfo.getName());
		return exploratoryService.start(userInfo, formDTO.getNotebookInstanceName());
	}

	/**
	 * Stops exploratory environment for user.
	 *
	 * @param userInfo user info.
	 * @param name     name of exploratory environment.
	 * @return Invocation response as JSON string.
	 */
	@DELETE
	@Path("/{name}/stop")
	@ApiOperation(value = "Stops notebook by name")
	public String stop(@ApiParam(hidden = true) @Auth UserInfo userInfo,
					   @ApiParam(value = "Notebook's name", required = true) @PathParam("name") String name) {
		log.debug("Stopping exploratory environment {} for user {}", name, userInfo.getName());
		return exploratoryService.stop(userInfo, name);
	}

	/**
	 * Terminates exploratory environment for user.
	 *
	 * @param userInfo user info.
	 * @param name     name of exploratory environment.
	 * @return Invocation response as JSON string.
	 */
	@DELETE
	@Path("/{name}/terminate")
	@ApiOperation(value = "Terminates notebook by name")
	public String terminate(@ApiParam(hidden = true) @Auth UserInfo userInfo,
							@ApiParam(value = "Notebook's name", required = true) @PathParam("name") String name) {
		log.debug("Terminating exploratory environment {} for user {}", name, userInfo.getName());
		return exploratoryService.terminate(userInfo, name);
	}

	private Exploratory getExploratory(@Valid @NotNull ExploratoryCreateFormDTO formDTO) {
		return Exploratory.builder()
				.name(formDTO.getName())
				.dockerImage(formDTO.getImage())
				.imageName(formDTO.getImageName())
				.templateName(formDTO.getTemplateName())
				.version(formDTO.getVersion())
				.shape(formDTO.getShape()).build();
	}
}
