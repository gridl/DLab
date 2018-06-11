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
package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.service.HandlerService;
import com.epam.dlab.dto.handlers.BaseCallbackHandlerDTO;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("handler")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class HandlerResource {

	@Inject
	private HandlerService handlerService;

	@POST
	@Path("/create")
	public Response create(BaseCallbackHandlerDTO dto) {
		handlerService.save(dto);
		return Response.ok().build();
	}

	@GET
	public Response getHandlers() {
		handlerService.sendAllHandlers();
		return Response.ok().build();
	}


}
