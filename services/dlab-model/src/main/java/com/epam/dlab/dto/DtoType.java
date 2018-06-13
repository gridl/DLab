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
package com.epam.dlab.dto;

public enum DtoType {

	COMPUTATIONAL_CREATE_AWS("computational_create_aws"),
	AWS_COMPUTATIONAL_TERMINATE("aws_computational_terminate"),
	COMPUTATIONAL_CREATE_GCP("computational_create_gcp"),
	GCP_COMPUTATIONAL_TERMINATE("gcp_computational_terminate"),
	COMPUTATIONAL_START("computational_start"),
	COMPUTATIONAL_STOP("computational_stop"),
	COMPUTATIONAL_TERMINATE("computational_terminate"),
	EXPLORATORY_IMAGE("exploratory_image"),
	LIBRARY_INSTALL("library_install"),
	ENV_BACKUP("env_backup"),
	REUPLOAD_KEY_CALLBACK("reupload_key_callback");

	private String name;

	DtoType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static DtoType of(String type) {
		if (type != null) {
			for (DtoType dt : DtoType.values()) {
				if (type.equalsIgnoreCase(dt.toString())) {
					return dt;
				}
			}
		}
		return null;
	}
}
