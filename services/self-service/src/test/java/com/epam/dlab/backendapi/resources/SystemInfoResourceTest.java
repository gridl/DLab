/*
 *
 *  * Copyright (c) 2018, EPAM SYSTEMS INC
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.resources.dto.SystemInfoDto;
import com.epam.dlab.backendapi.service.SystemInfoService;
import com.epam.dlab.model.systeminfo.DiskInfo;
import com.epam.dlab.model.systeminfo.MemoryInfo;
import com.epam.dlab.model.systeminfo.OsInfo;
import com.epam.dlab.model.systeminfo.ProcessorInfo;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SystemInfoResourceTest extends TestBase {

	private SystemInfoService systemInfoService = mock(SystemInfoService.class);

	@Rule
	public final ResourceTestRule resources = getResourceTestRuleInstance(new SystemInfoResource(systemInfoService));

	@Before
	public void setup() throws AuthenticationException {
		authSetup();
	}

	@Test
	public void getSystemInfo() {
		when(systemInfoService.getSystemInfo()).thenReturn(getSystemInfoDto());
		final Response response = resources.getJerseyTest()
				.target("/sysinfo")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_OK, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verify(systemInfoService).getSystemInfo();
		verifyNoMoreInteractions(systemInfoService);
	}

	@Test
	public void getSystemInfoWithFailedAuth() throws AuthenticationException {
		authFailSetup();
		when(systemInfoService.getSystemInfo()).thenReturn(getSystemInfoDto());
		final Response response = resources.getJerseyTest()
				.target("/sysinfo")
				.request()
				.header("Authorization", "Bearer " + TOKEN)
				.get();

		assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

		verifyZeroInteractions(systemInfoService);
	}

	private SystemInfoDto getSystemInfoDto() {
		OsInfo osInfo = OsInfo.builder()
				.family(System.getProperty("os.name"))
				.buildNumber(System.getProperty("os.version"))
				.build();
		ProcessorInfo processorInfo = ProcessorInfo.builder().build();
		MemoryInfo memoryInfo = MemoryInfo.builder().build();
		DiskInfo diskInfo = DiskInfo.builder().build();
		return new SystemInfoDto(osInfo, processorInfo, memoryInfo, Collections.singletonList(diskInfo));
	}
}