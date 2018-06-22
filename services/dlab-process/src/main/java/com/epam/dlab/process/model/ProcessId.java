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

public class ProcessId {

    private final String user;
	private final String uuid;

	public ProcessId(String user, String uuid) {
        this.user = user;
		this.uuid = uuid;
    }

	public String getUuid() {
		return uuid;
    }

    public String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessId processId = (ProcessId) o;

        if (user != null ? !user.equals(processId.user) : processId.user != null) return false;
		return uuid != null ? uuid.equals(processId.uuid) : processId.uuid == null;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
		result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProcessId{" +
                "user='" + user + '\'' +
				", uuid='" + uuid + '\'' +
                '}';
    }
}
