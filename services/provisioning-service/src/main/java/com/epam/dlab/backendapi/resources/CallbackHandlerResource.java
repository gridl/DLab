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

import com.epam.dlab.backendapi.service.CallbackHandlerService;
import com.epam.dlab.dto.handlers.BaseCallbackHandlerDTO;
import com.epam.dlab.rest.contracts.HandlerAPI;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path(HandlerAPI.HANDLER_PS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class CallbackHandlerResource {

	@Inject
	private CallbackHandlerService callbackHandlerService;

	@POST
	@SuppressWarnings("unchecked")
	public Response receiveHandlers(Object handlerList) {
		callbackHandlerService.startHandlers((List<BaseCallbackHandlerDTO>) handlerList);
		return Response.ok().build();
	}
}
