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
import com.epam.dlab.backendapi.resources.dto.HealthStatusEnum;
import com.epam.dlab.backendapi.resources.dto.HealthStatusPageDTO;
import com.epam.dlab.backendapi.resources.dto.InfrastructureInfo;
import com.epam.dlab.dto.base.edge.EdgeInfo;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.process.model.ProcessData;
import com.epam.dlab.process.model.ProcessInfo;
import com.epam.dlab.process.model.ProcessStatus;
import com.epam.dlab.process.model.ProcessType;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.InfrasctructureAPI;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InfrastructureManagementServiceBaseTest {

	private final String USER = "test";

	@Mock
	private EnvStatusDAO envDAO;
	@Mock
	private ExploratoryDAO expDAO;
	@Mock
	private KeyDAO keyDAO;
	@Mock
	private RESTService provisioningService;
	@Mock
	private SelfServiceApplicationConfiguration configuration;

	@InjectMocks
	private InfrastructureManagementServiceBase infrastructureInfoServiceBase = spy
			(InfrastructureManagementServiceBase.class);

	@Test
	public void getUserResources() throws NoSuchFieldException, IllegalAccessException {
		Iterable<Document> documents = Collections.singletonList(new Document());
		when(expDAO.findExploratory(anyString())).thenReturn(documents);

		EdgeInfo edgeInfo = new EdgeInfo();
		edgeInfo.setInstanceId("someId");
		edgeInfo.setEdgeStatus("someStatus");
		when(keyDAO.getEdgeInfo(anyString())).thenReturn(edgeInfo);

		InfrastructureInfo expectedInfrastructureInfo = new InfrastructureInfo(Collections.emptyMap(), documents);
		InfrastructureInfo actualInfrastructureInfo = infrastructureInfoServiceBase.getUserResources(USER);
		assertNotNull(actualInfrastructureInfo);
		assertTrue(areInfrastructureInfoObjectsEqual(actualInfrastructureInfo, expectedInfrastructureInfo));

		verify(expDAO).findExploratory(USER);
		verify(keyDAO).getEdgeInfo(USER);
		verifyNoMoreInteractions(expDAO, keyDAO);
	}

	@Test
	public void getUserResourcesWhenMethodGetEdgeInfoThrowsException() {
		Iterable<Document> documents = Collections.singletonList(new Document());
		when(expDAO.findExploratory(anyString())).thenReturn(documents);

		EdgeInfo edgeInfo = new EdgeInfo();
		edgeInfo.setInstanceId("someId");
		edgeInfo.setEdgeStatus("someStatus");
		doThrow(new DlabException("Edge info not found")).when(keyDAO).getEdgeInfo(anyString());

		try {
			infrastructureInfoServiceBase.getUserResources(USER);
		} catch (DlabException e) {
			assertEquals("Could not load list of provisioned resources for user: ", e.getMessage());
		}
		verify(expDAO).findExploratory(USER);
		verify(keyDAO).getEdgeInfo(USER);
		verifyNoMoreInteractions(expDAO, keyDAO);
	}

	@Test
	public void getHeathStatus() {
		when(envDAO.getHealthStatusPageDTO(anyString(), anyBoolean())).thenReturn(new HealthStatusPageDTO()
				.withStatus(HealthStatusEnum.OK));
		when(configuration.isBillingSchedulerEnabled()).thenReturn(false);

		HealthStatusPageDTO actualHealthStatusPageDTO =
				infrastructureInfoServiceBase.getHeathStatus(USER, false, true);
		assertNotNull(actualHealthStatusPageDTO);
		assertEquals(HealthStatusEnum.OK.toString(), actualHealthStatusPageDTO.getStatus());
		assertFalse(actualHealthStatusPageDTO.isBillingEnabled());
		assertTrue(actualHealthStatusPageDTO.isAdmin());

		verify(envDAO).getHealthStatusPageDTO(USER, false);
		verify(configuration).isBillingSchedulerEnabled();
		verifyNoMoreInteractions(envDAO, configuration);
	}

	@Test
	public void getHeathStatusWhenMethodGetHealthStatusPageDTOThrowsException() {
		doThrow(new DlabException("Cannot fetch health status!"))
				.when(envDAO).getHealthStatusPageDTO(anyString(), anyBoolean());
		try {
			infrastructureInfoServiceBase.getHeathStatus(USER, false, false);
		} catch (DlabException e) {
			assertEquals("Cannot fetch health status!", e.getMessage());
		}
		verify(envDAO).getHealthStatusPageDTO(USER, false);
		verifyNoMoreInteractions(envDAO);
	}

	@Test
	public void getProcessInfo() {
		when(provisioningService.get(anyString(), anyString(), any())).thenReturn(getProcessInfoList());

		infrastructureInfoServiceBase.getProcessInfo(new UserInfo(USER, "token"));

		verify(provisioningService).get(InfrasctructureAPI.INFRASTRUCTURE_OPERATIONS, "token", List.class);
		verifyNoMoreInteractions(provisioningService);
	}

	@Test
	public void cancelProcess() {
		when(provisioningService.delete(anyString(), anyString(), any())).thenReturn(mock(Response.class));

		infrastructureInfoServiceBase.cancelProcess(new UserInfo(USER, "token"), "someUuid");

		verify(provisioningService).delete(InfrasctructureAPI.INFRASTRUCTURE_OPERATIONS_CANCEL + "someUuid",
				"token", Response.class);
		verifyNoMoreInteractions(provisioningService);
	}

	private boolean areInfrastructureInfoObjectsEqual(InfrastructureInfo object1, InfrastructureInfo object2) throws
			NoSuchFieldException, IllegalAccessException {
		Field shared1 = object1.getClass().getDeclaredField("shared");
		shared1.setAccessible(true);
		Field shared2 = object2.getClass().getDeclaredField("shared");
		shared2.setAccessible(true);
		Field exploratory1 = object1.getClass().getDeclaredField("exploratory");
		exploratory1.setAccessible(true);
		Field exploratory2 = object2.getClass().getDeclaredField("exploratory");
		exploratory2.setAccessible(true);
		return shared1.get(object1).equals(shared2.get(object2))
				&& exploratory1.get(object1).equals(exploratory2.get(object2));
	}

	private List<ProcessInfo> getProcessInfoList() {
		ProcessData pd1 = new ProcessData(USER, "uuid1", ProcessType.BACKUP_CREATE, "descr1");
		ProcessData pd2 = new ProcessData(USER, "uuid2", ProcessType.EDGE_CREATE, "descr2");
		ProcessInfo pi1 = new ProcessInfo(pd1, ProcessStatus.CREATED, new String[0], "stdOut1", "stdErr1",
				0, 100L, 200L, Collections.emptyList(), 10);
		ProcessInfo pi2 = new ProcessInfo(pd2, ProcessStatus.CREATED, new String[0], "stdOut2", "stdErr2",
				0, 100L, 200L, Collections.emptyList(), 20);
		return Arrays.asList(pi1, pi2);
	}
}
