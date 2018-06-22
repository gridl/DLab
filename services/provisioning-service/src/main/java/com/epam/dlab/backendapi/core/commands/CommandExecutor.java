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

import com.epam.dlab.process.model.DlabProcess;
import com.epam.dlab.process.model.ProcessId;
import com.epam.dlab.process.model.ProcessInfo;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
@Singleton
public class CommandExecutor implements ICommandExecutor {

	public ProcessInfo startSync(final String username, final String uuid, String command) throws ExecutionException,
			InterruptedException {
		return DlabProcess.getInstance().start(new ProcessId(username, uuid), "bash", "-c", command).get();
	}

	public void startAsync(final String username, final String uuid, final String command) {
		DlabProcess.getInstance().start(new ProcessId(username, uuid), "bash", "-c", command);
	}

	public Boolean cancelSync(final String username, final String uuid) throws ExecutionException,
			InterruptedException {
		return DlabProcess.getInstance().cancel(username, uuid).get();
	}

	public void cancelAsync(final String username, final String uuid) {
		DlabProcess.getInstance().cancel(username, uuid);
	}

}