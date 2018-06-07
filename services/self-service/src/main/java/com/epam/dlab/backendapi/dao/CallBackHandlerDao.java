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
package com.epam.dlab.backendapi.dao;

import com.epam.dlab.dto.handlers.BaseCallbackHandlerDTO;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static com.epam.dlab.backendapi.dao.MongoCollections.CALLBACK_HANDLERS;

@Slf4j
@Singleton
public class CallBackHandlerDao extends BaseDAO {

	static final String HANDLER_TYPE = "handler_type";
	static final String DOCKER_ACTION = "docker_action";
	static final String UUID = "uuid";
	static final String CALLBACK_URI = "callback_uri";
	static final String EXPLORATORY_NAME = "exploratory_name";
	static final String DTO = "data_transfer_object";

	/**
	 * Inserts the info about callback handler into Mongo database.
	 *
	 * @param dto the info about callback handler
	 */
	public <T extends BaseCallbackHandlerDTO> void insertCallbackHandler(T dto) {
		insertOne(CALLBACK_HANDLERS, dto);
	}

}
