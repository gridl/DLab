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
package com.epam.dlab.backendapi.service.impl;

import com.epam.dlab.backendapi.dao.CallBackHandlerDao;
import com.epam.dlab.backendapi.service.HandlerService;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.dto.handlers.BaseCallbackHandlerDTO;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.HandlerAPI;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
public class HandlerServiceImpl implements HandlerService {

	@Inject
	private CallBackHandlerDao dao;

	@Inject
	@Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
	private RESTService provisioningService;


	@Override
	public void save(BaseCallbackHandlerDTO dto) {
		log.debug("Saving callback handler data {} to database...", dto);
		dao.insertCallbackHandler(dto);
	}

	@Override
	public void sendAllHandlers() {
		List<BaseCallbackHandlerDTO> handlerList = dao.getCallbackHandlers();
		log.debug("Sending list with {} callback handlers to provisioning service...", handlerList.size());
		provisioningService.post(HandlerAPI.HANDLER_PS, handlerList, Response.class);
	}


}
