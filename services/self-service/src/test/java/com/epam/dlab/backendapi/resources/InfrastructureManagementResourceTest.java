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
import com.epam.dlab.backendapi.resources.dto.HealthStatusPageDTO;
import com.epam.dlab.backendapi.resources.dto.InfrastructureInfo;
import com.epam.dlab.backendapi.service.InfrastructureManagementService;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.process.model.ProcessData;
import com.epam.dlab.process.model.ProcessInfo;
import com.epam.dlab.process.model.ProcessStatus;
import com.epam.dlab.process.model.ProcessType;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class InfrastructureManagementResourceTest extends TestBase {

	private InfrastructureManagementService infrastructureManagementService =
			mock(InfrastructureManagementService.class);

	@Rule
	public final ResourceTestRule resources =
			getResourceTestRuleInstance(new InfrastructureManagementResource(infrastructureManagementService));

	@Before
	public void setup() throws AuthenticationException {
		authSetup();
	}

	@Test
	public void status() {
		final Response response = resources.getJerseyTest()
				.target("/infrastructure")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verifyZeroInteractions(infrastructureManagementService);
	}

	@Test
	public void statusWithFailedAuth() throws AuthenticationException {
		authFailSetup();
		final Response response = resources.getJerseyTest()
				.target("/infrastructure")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verifyZeroInteractions(infrastructureManagementService);
	}

	@Test
	public void healthStatus() {
		HealthStatusPageDTO hspDto = getHealthStatusPageDTO();
		when(infrastructureManagementService.getHeathStatus(anyString(), anyBoolean(), anyBoolean())).thenReturn
				(hspDto);
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/status")
				.queryParam("full", "1")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(hspDto.getStatus(), response.readEntity(HealthStatusPageDTO.class).getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getHeathStatus(eq(USER.toLowerCase()), eq(true), anyBoolean());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void healthStatusWithFailedAuth() throws AuthenticationException {
		authFailSetup();
		HealthStatusPageDTO hspDto = getHealthStatusPageDTO();
		when(infrastructureManagementService.getHeathStatus(anyString(), anyBoolean(), anyBoolean())).thenReturn(hspDto);
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/status")
				.queryParam("full", "1")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(hspDto.getStatus(), response.readEntity(HealthStatusPageDTO.class).getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getHeathStatus(eq(USER.toLowerCase()), eq(true), anyBoolean());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void healthStatusWithDefaultQueryParam() {
		HealthStatusPageDTO hspDto = getHealthStatusPageDTO();
		when(infrastructureManagementService.getHeathStatus(anyString(), anyBoolean(), anyBoolean())).thenReturn(hspDto);
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/status")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(hspDto.getStatus(), response.readEntity(HealthStatusPageDTO.class).getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getHeathStatus(eq(USER.toLowerCase()), eq(false), anyBoolean());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void healthStatusWithException() {
		doThrow(new DlabException("Could not return status of resources for user"))
				.when(infrastructureManagementService).getHeathStatus(anyString(), anyBoolean(), anyBoolean());
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/status")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getHeathStatus(eq(USER.toLowerCase()), eq(false), anyBoolean());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void getUserResources() {
		InfrastructureInfo info = getInfrastructureInfo();
		when(infrastructureManagementService.getUserResources(anyString())).thenReturn(info);
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/info")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(info.toString(), response.readEntity(InfrastructureInfo.class).toString());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getUserResources(USER.toLowerCase());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void getUserResourcesWithFailedAuth() throws AuthenticationException {
		authFailSetup();
		InfrastructureInfo info = getInfrastructureInfo();
		when(infrastructureManagementService.getUserResources(anyString())).thenReturn(info);
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/info")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(info.toString(), response.readEntity(InfrastructureInfo.class).toString());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getUserResources(USER.toLowerCase());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void getUserResourcesWithException() {
		doThrow(new DlabException("Could not load list of provisioned resources for user"))
				.when(infrastructureManagementService).getUserResources(anyString());
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/info")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getUserResources(USER.toLowerCase());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void operations() {
		List<ProcessInfo> processInfo = getProcessInfo();
		when(infrastructureManagementService.getProcessInfo(any(UserInfo.class))).thenReturn(processInfo);
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/operations")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		String expected = "[{uuid=uuid1, process_type=Backup_creating, process_description=descr1, status=CREATED}, " +
				"{uuid=uuid2, process_type=Edge_creating, process_description=descr2, status=CREATED}]";
		assertEquals(expected, response.readEntity(List.class).toString());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getProcessInfo(getUserInfo());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void operationsWithFailedAuth() throws AuthenticationException {
		authFailSetup();
		List<ProcessInfo> processInfo = getProcessInfo();
		when(infrastructureManagementService.getProcessInfo(any(UserInfo.class))).thenReturn(processInfo);
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/operations")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		String expected = "[{uuid=uuid1, process_type=Backup_creating, process_description=descr1, status=CREATED}, " +
				"{uuid=uuid2, process_type=Edge_creating, process_description=descr2, status=CREATED}]";
		assertEquals(expected, response.readEntity(List.class).toString());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getProcessInfo(getUserInfo());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void operationsWithException() {
		doThrow(new DlabException("Could not load list of processes for user"))
				.when(infrastructureManagementService).getProcessInfo(any(UserInfo.class));
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/operations")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).getProcessInfo(getUserInfo());
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void cancel() {
		doNothing().when(infrastructureManagementService).cancelProcess(any(UserInfo.class), anyString());
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/operations/cancel/someUuid")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.delete();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).cancelProcess(getUserInfo(), "someUuid");
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void cancelWithFailedAuth() throws AuthenticationException {
		authFailSetup();
		doNothing().when(infrastructureManagementService).cancelProcess(any(UserInfo.class), anyString());
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/operations/cancel/someUuid")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.delete();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertNull(response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).cancelProcess(getUserInfo(), "someUuid");
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	@Test
	public void cancelWithException() {
		doThrow(new DlabException("Could not cancel process for user"))
				.when(infrastructureManagementService).cancelProcess(any(UserInfo.class), anyString());
		final Response response = resources.getJerseyTest()
				.target("/infrastructure/operations/cancel/someUuid")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.delete();

		assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(infrastructureManagementService).cancelProcess(getUserInfo(), "someUuid");
		verifyNoMoreInteractions(infrastructureManagementService);
	}

	private HealthStatusPageDTO getHealthStatusPageDTO() {
		HealthStatusPageDTO hspdto = new HealthStatusPageDTO();
		hspdto.setStatus("someStatus");
		return hspdto;
	}

	private InfrastructureInfo getInfrastructureInfo() {
		return new InfrastructureInfo(Collections.emptyMap(), Collections.emptyList());
	}

	private List<ProcessInfo> getProcessInfo() {
		ProcessData pd1 = new ProcessData(USER, "uuid1", ProcessType.BACKUP_CREATE, "descr1");
		ProcessData pd2 = new ProcessData(USER, "uuid2", ProcessType.EDGE_CREATE, "descr2");
		ProcessInfo pi1 = new ProcessInfo(pd1, ProcessStatus.CREATED, new String[0], "stdOut1", "stdErr1",
				0, 100L, 200L, Collections.emptyList(), 10);
		ProcessInfo pi2 = new ProcessInfo(pd2, ProcessStatus.CREATED, new String[0], "stdOut2", "stdErr2",
				0, 100L, 200L, Collections.emptyList(), 20);
		return Arrays.asList(pi1, pi2);
	}
}
