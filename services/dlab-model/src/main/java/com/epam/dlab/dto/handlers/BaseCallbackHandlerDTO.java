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
package com.epam.dlab.dto.handlers;

import com.epam.dlab.dto.handlers.transferobjects.TransferData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseCallbackHandlerDTO<T extends BaseCallbackHandlerDTO> {

	@SuppressWarnings("unchecked")
	private final T self = (T) this;

	@JsonProperty("_id")
	private String id;

	@JsonProperty("handler_type")
	private String handlerType;

	@JsonProperty("docker_action")
	private String dockerAction;

	@JsonProperty("uuid")
	private String uuid;

	@JsonProperty("user")
	private String user;

	@JsonProperty("data_transfer_object")
	private TransferData transferData;

	public T withId(String id) {
		setId(id);
		return self;
	}

	public T withHandlerType(String handlerType) {
		setHandlerType(handlerType);
		return self;
	}

	public T withDockerAction(String dockerAction) {
		setDockerAction(dockerAction);
		return self;
	}

	public T withUuid(String uuid) {
		setUuid(uuid);
		return self;
	}

	public T withUser(String user) {
		setUser(user);
		return self;
	}

	public T withTransferData(TransferData transferData) {
		setTransferData(transferData);
		return self;
	}

}
