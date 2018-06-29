/*
 * Copyright (c) 2018, EPAM SYSTEMS INC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.commands.ICommandExecutor;
import com.epam.dlab.backendapi.core.commands.PythonBackupCommand;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.backendapi.core.response.handlers.BackupCallbackHandler;
import com.epam.dlab.backendapi.service.InfrastructureCallbackHandlerService;
import com.epam.dlab.backendapi.service.SelfServiceHelper;
import com.epam.dlab.dto.backup.EnvBackupDTO;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.epam.dlab.rest.contracts.BackupAPI;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(BackupAPI.BACKUP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class BackupResource {

	@Inject
	private ProvisioningServiceApplicationConfiguration configuration;
	@Inject
	protected FolderListenerExecutor folderListenerExecutor;
	@Inject
	protected ICommandExecutor commandExecutor;
	@Inject
	protected SelfServiceHelper selfServiceHelper;
	@Inject
	protected InfrastructureCallbackHandlerService infrastructureCallbackHandlerService;


	@POST
	public Response createBackup(@Auth UserInfo ui, EnvBackupDTO dto) {
		folderListenerExecutor.start(configuration.getBackupDirectory(), configuration.getProcessTimeout(),
				new BackupCallbackHandler(selfServiceHelper, infrastructureCallbackHandlerService,
						ApiCallbacks.BACKUP_URI, ui.getName(), dto));
		String command = new PythonBackupCommand(configuration.getBackupScriptPath())
				.withConfig(dto.getConfigFiles())
				.withJars(dto.getJars())
				.withKeys(dto.getKeys())
				.withDBBackup(dto.isDatabaseBackup())
				.withLogsBackup(dto.isLogsBackup())
				.withResponsePath(configuration.getBackupDirectory())
				.withRequestId(dto.getId())
				.withSystemUser()
				.withCertificates(dto.getCertificates()).toCMD();
		commandExecutor.executeAsync(ui.getName(), dto.getId(), command);
		return Response.accepted(dto.getId()).build();
	}
}
