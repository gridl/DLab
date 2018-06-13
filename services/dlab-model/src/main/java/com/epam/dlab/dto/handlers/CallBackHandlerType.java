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

public enum CallBackHandlerType {
	BACKUP_HANDLER("backup_handler"),
	COMPUTATIONAL_HANDLER("computational_handler"),
	EDGE_HANDLER("edge_handler"),
	EXPLORATORY_HANDLER("exploratory_handler"),
	EXPLORATORY_GIT_CREDS_HANDLER("exploratory_git_creds_handler"),
	IMAGE_CREATE_HANDLER("image_create_handler"),
	LIB_INSTALL_HANDLER("lib_install_handler"),
	LIB_LIST_HANDLER("lib_list_handler"),
	RESOURCES_STATUS_HANDLER("resources_status_handler"),
	REUPLOAD_KEY_HANDLER("reupload_key_handler");

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
			for (CallBackHandlerType cbt : CallBackHandlerType.values()) {
				if (type.equalsIgnoreCase(cbt.toString())) {
					return cbt;
				}
			}
		}
		return null;
	}

}
