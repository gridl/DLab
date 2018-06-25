/*
Copyright 2016 EPAM Systems, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.epam.dlab.process.model;

import com.epam.dlab.process.serializer.ProcessTypeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ProcessData {

	@JsonIgnore
	private final String user;

	private final String uuid;

	@JsonProperty("process_type")
	@JsonSerialize(using = ProcessTypeSerializer.class)
	private final ProcessType processType;

	@JsonProperty("process_description")
	private final String processDescription;

	public ProcessData(String user, String uuid, ProcessType processType, String processDescription) {
		this.user = user;
		this.uuid = uuid;
		this.processType = processType;
		this.processDescription = processDescription;
	}

	public ProcessData(String user, String uuid) {
		this(user, uuid, null, null);
	}

	public String getUuid() {
		return uuid;
	}

	public String getUser() {
		return user;
	}

	public ProcessType getProcessType() {
		return processType;
	}

	public String getProcessDescription() {
		return processDescription;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ProcessData processData = (ProcessData) o;

		if (user != null ? !user.equals(processData.user) : processData.user != null) return false;
		return uuid != null ? uuid.equals(processData.uuid) : processData.uuid == null;
	}

	@Override
	public int hashCode() {
		int result = user != null ? user.hashCode() : 0;
		result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ProcessData{" +
				"user='" + user + '\'' +
				", uuid='" + uuid + '\'' +
				'}';
	}
}
