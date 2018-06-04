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
package com.epam.dlab.backendapi.core.response.handlers;

public enum CallBackHandlerType {
	BACKUP("backup"),
	COMPUTATIONAL("computational"),
	EDGE("edge"),
	EXPLORATORY("exploratory"),
	EXPLORATORY_GIT_CREDS("exploratory_git_creds"),
	IMAGE_CREATE("image_create"),
	LIB_INSTALL("lib_install"),
	LIB_LIST("lib_list"),
	RESOURCES_STATUS("resources_status"),
	REUPLOAD_KEY("reupload_key");

	private String name;

	CallBackHandlerType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static CallBackHandlerType of(String type) {
		if (type != null) {
			for (CallBackHandlerType uis : CallBackHandlerType.values()) {
				if (type.equalsIgnoreCase(uis.toString())) {
					return uis;
				}
			}
		}
		return null;
	}

}
