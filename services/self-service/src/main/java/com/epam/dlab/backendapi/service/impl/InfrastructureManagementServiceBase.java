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

package com.epam.dlab.backendapi.service.impl;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.backendapi.dao.EnvStatusDAO;
import com.epam.dlab.backendapi.dao.ExploratoryDAO;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.backendapi.resources.dto.HealthStatusPageDTO;
import com.epam.dlab.backendapi.resources.dto.InfrastructureInfo;
import com.epam.dlab.backendapi.service.InfrastructureManagementService;
import com.epam.dlab.dto.base.edge.EdgeInfo;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.process.model.ProcessInfo;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.InfrasctructureAPI;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.epam.dlab.constants.ServiceConsts.PROVISIONING_SERVICE_NAME;

@Slf4j
public abstract class InfrastructureManagementServiceBase<T> implements InfrastructureManagementService {

	@Inject
	private ExploratoryDAO expDAO;
	@Inject
	private KeyDAO keyDAO;
	@Inject
	private EnvStatusDAO envDAO;
	@Inject
	private SelfServiceApplicationConfiguration configuration;
	@Inject
	@Named(PROVISIONING_SERVICE_NAME)
	private RESTService provisioningService;


	@SuppressWarnings("unchecked")
	private Map<String, String> getSharedInfo(EdgeInfo edgeInfo) {
		return getSharedInfo((T) edgeInfo);
	}

	@Override
	public InfrastructureInfo getUserResources(String user) {
		log.debug("Loading list of provisioned resources for user {}", user);
		try {
			Iterable<Document> documents = expDAO.findExploratory(user);
			EdgeInfo edgeInfo = keyDAO.getEdgeInfo(user);
			return new InfrastructureInfo(getSharedInfo(edgeInfo), documents);
		} catch (Exception e) {
			log.error("Could not load list of provisioned resources for user: {}", user, e);
			throw new DlabException("Could not load list of provisioned resources for user: ");
		}
	}

	@Override
	public HealthStatusPageDTO getHeathStatus(String user, boolean fullReport, boolean isAdmin) {
		log.debug("Request the status of resources for user {}, report type {}", user, fullReport);
		try {
			return envDAO.getHealthStatusPageDTO(user, fullReport)
					.withBillingEnabled(configuration.isBillingSchedulerEnabled())
					.withAdmin(isAdmin);
		} catch (Exception e) {
			log.warn("Could not return status of resources for user {}: {}", user, e.getLocalizedMessage(), e);
			throw new DlabException(e.getMessage(), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ProcessInfo> getProcessInfo(UserInfo userInfo) {
		log.debug("Fetching info about current processes...");
		return provisioningService.get(InfrasctructureAPI.INFRASTRUCTURE_OPERATIONS,
				userInfo.getAccessToken(), List.class);
	}

	@Override
	public void cancelProcess(UserInfo userInfo, String uuid) {
		log.debug("Cancelling the process with id {}...", uuid);
		provisioningService.delete(InfrasctructureAPI.INFRASTRUCTURE_OPERATIONS_CANCEL + uuid,
				userInfo.getAccessToken(), Response.class);
	}

	protected abstract Map<String, String> getSharedInfo(T sharedInfo);
}
