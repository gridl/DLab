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
import com.epam.dlab.backendapi.dao.UserSettingsDAO;
import com.epam.dlab.backendapi.swagger.SwaggerConfigurator;
import com.epam.dlab.exceptions.DlabException;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/user/settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "User's settings service", authorizations = @Authorization(SwaggerConfigurator.TOKEN_AUTH))
public class UserSettingsResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserSettingsResource.class);

	private UserSettingsDAO userSettingsDAO;

	@Inject
	public UserSettingsResource(UserSettingsDAO userSettingsDAO) {
		this.userSettingsDAO = userSettingsDAO;
	}
    
    @GET
	@ApiOperation(value = "Returns user's settings")
	public String getSettings(@ApiParam(hidden = true) @Auth UserInfo userInfo) {
    	String settings = userSettingsDAO.getUISettings(userInfo);
    	LOGGER.debug("Returns settings for user {}, content is {}", userInfo.getName(), settings);
        return settings;
    }
    
    @POST
	@ApiOperation(value = "Saves user's settings to database")
	@ApiResponses(value = @ApiResponse(code = 200, message = "User's settings were saved to database successfully"))
	public Response saveSettings(@ApiParam(hidden = true) @Auth UserInfo userInfo,
								 @ApiParam(value = "Settings data", required = true)
								 @NotBlank String settings) {
        LOGGER.debug("Saves settings for user {}, content is {}", userInfo.getName(), settings);
        try {
        	userSettingsDAO.setUISettings(userInfo, settings);
        } catch (Exception e) {
        	LOGGER.error("Save settings for user {} fail", userInfo.getName(), e);
        	throw new DlabException("Save settings for user " + userInfo.getName() + " fail: " + e.getLocalizedMessage(), e);
        }
        return Response.ok().build();
    }
}
