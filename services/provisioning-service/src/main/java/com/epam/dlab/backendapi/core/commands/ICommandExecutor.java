/***************************************************************************

 Copyright (c) 2016, EPAM SYSTEMS INC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 ****************************************************************************/
package com.epam.dlab.backendapi.core.commands;

import com.epam.dlab.process.model.ProcessInfo;
import com.epam.dlab.process.model.ProcessType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ICommandExecutor {

	ProcessInfo startSync(String username, String uuid, ProcessType processType, String processDescription,
						  String command) throws ExecutionException, InterruptedException;

	void startAsync(String username, String uuid, ProcessType processType, String processDescription, String command);

	default Boolean cancelSync(String username, String uuid) throws ExecutionException, InterruptedException {
		return false;
	}

	default void cancelAsync(String username, String uuid) {
	}

	default List<ProcessInfo> getProcessInfo(String username) {
		return Collections.emptyList();
	}

}
