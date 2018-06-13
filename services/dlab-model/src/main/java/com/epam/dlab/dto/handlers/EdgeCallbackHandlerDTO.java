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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdgeCallbackHandlerDTO extends BaseCallbackHandlerDTO {

	@JsonProperty("callback_uri")
	private String callbackUri;

	@JsonProperty("response_class")
	private String responseClass;

	@JsonProperty("enclosing_class")
	private String enclosingClass;


	public EdgeCallbackHandlerDTO withCallbackUri(String callbackUri) {
		setCallbackUri(callbackUri);
		return this;
	}

	public EdgeCallbackHandlerDTO withResponseClass(String responseClass) {
		setResponseClass(responseClass);
		return this;
	}

	public EdgeCallbackHandlerDTO withEnclosingClass(String enclosingClass) {
		setEnclosingClass(enclosingClass);
		return this;
	}
}
