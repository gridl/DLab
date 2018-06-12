/*
 * Copyright (c) 2017, EPAM SYSTEMS INC
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
import com.epam.dlab.process.model.DlabProcess;
import com.epam.dlab.process.model.ProcessId;
import com.epam.dlab.rest.contracts.InfrasctructureAPI;
import io.dropwizard.auth.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Path(InfrasctructureAPI.INFRASTRUCTURE)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InfrastructureResource {

	/**
	 * Return status of provisioning service.
	 */
	@GET
	public Response status(@Auth UserInfo ui) {
		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("/operations")
	public Response operations(@Auth UserInfo ui){
		return Response.ok(DlabProcess.getInstance().getActiveProcesses(ui.getName())).build();
	}

	@DELETE
	@Path("/operations")
	public Response kill(@Auth UserInfo ui, ProcessId processId) throws ExecutionException, InterruptedException {
		DlabProcess.getInstance().kill(processId).get();
		return Response.ok().build();
	}

}
