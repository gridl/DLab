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

import com.epam.dlab.process.exception.DlabProcessException;
import com.epam.dlab.process.model.DlabProcess;
import com.epam.dlab.process.model.ProcessInfo;
import com.epam.dlab.process.model.ProcessStatus;
import com.epam.dlab.process.model.ProcessType;
import com.google.inject.Singleton;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Singleton
public class CommandExecutor implements ICommandExecutor {

	@Override
	public ProcessInfo startSync(final String username, final String uuid, final ProcessType processType,
								 final String processDescription, final String command)
			throws ExecutionException, InterruptedException {
		return DlabProcess.getInstance().start(username, uuid, processType, processDescription,
				"bash", "-c", command).get();
	}

	@Override
	public void startAsync(final String username, final String uuid, final ProcessType processType,
						   final String processDescription, final String command) {
		DlabProcess.getInstance().start(username, uuid, processType, processDescription, "bash", "-c", command);
	}

	@Override
	public Boolean cancelSync(final String username, final String uuid) throws ExecutionException,
			InterruptedException {
		ProcessStatus processStatus = DlabProcess.getInstance().getProcessStatus(username, uuid);
		if (processStatus == ProcessStatus.LAUNCHING) {
			return DlabProcess.getInstance().kill(username, uuid).get();
		} else {
			throw new DlabProcessException("Couldn't cancel the process with ID " + uuid + " for user " + username +
					" because its current status is " + processStatus);
		}
	}

	@Override
	public void cancelAsync(final String username, final String uuid) {
		ProcessStatus processStatus = DlabProcess.getInstance().getProcessStatus(username, uuid);
		if (processStatus == ProcessStatus.LAUNCHING) {
			DlabProcess.getInstance().kill(username, uuid);
		} else {
			throw new DlabProcessException("Couldn't cancel the process with ID " + uuid + " for user " + username +
					" because its current status is " + processStatus);
		}
	}

	@Override
	public List<ProcessInfo> getProcessInfo(String username) {
		return DlabProcess.getInstance().getProcessInfoData(username);
	}

}