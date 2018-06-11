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

import com.epam.dlab.dto.handlers.BaseCallbackHandlerDTO;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.HandlerAPI;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.xml.ws.Response;
import java.util.List;

@Slf4j
@Singleton
public class CallbackHandlerService {

	@Inject
	private RESTService selfService;

	public void run() {
		log.debug("Calling self-service for getting all callback handlers...");
		selfService.get(HandlerAPI.HANDLER_SS, Response.class);
	}

	public void startHandlers(List<BaseCallbackHandlerDTO> handlerList) {
		log.debug("Restarting all callback handlers...");
	}

}
