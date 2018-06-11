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
import com.fasterxml.jackson.annotation.*;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "class_name")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ReuploadKeyCallbackHandlerDTO.class, name = "ReuploadKeyCallbackHandlerDTO"),
		@JsonSubTypes.Type(value = LibListCallbackHandlerDTO.class, name = "LibListCallbackHandlerDTO"),
		@JsonSubTypes.Type(value = ExploratoryGitCredsCallbackHandlerDTO.class, name =
				"ExploratoryGitCredsCallbackHandlerDTO"),
		@JsonSubTypes.Type(value = ExploratoryCallbackHandlerDTO.class, name = "ExploratoryCallbackHandlerDTO"),
		@JsonSubTypes.Type(value = EdgeCallbackHandlerDTO.class, name = "EdgeCallbackHandlerDTO"),
		@JsonSubTypes.Type(value = BackupCallbackHandlerDTO.class, name = "BackupCallbackHandlerDTO")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseCallbackHandlerDTO {

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

	public BaseCallbackHandlerDTO withId(String id) {
		setId(id);
		return this;
	}

	public BaseCallbackHandlerDTO withHandlerType(String handlerType) {
		setHandlerType(handlerType);
		return this;
	}

	public BaseCallbackHandlerDTO withDockerAction(String dockerAction) {
		setDockerAction(dockerAction);
		return this;
	}

	public BaseCallbackHandlerDTO withUuid(String uuid) {
		setUuid(uuid);
		return this;
	}

	public BaseCallbackHandlerDTO withUser(String user) {
		setUser(user);
		return this;
	}

	public BaseCallbackHandlerDTO withTransferData(TransferData transferData) {
		setTransferData(transferData);
		return this;
	}

}
