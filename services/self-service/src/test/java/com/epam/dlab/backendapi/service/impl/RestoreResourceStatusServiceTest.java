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
package com.epam.dlab.backendapi.service.impl;

import com.epam.dlab.backendapi.dao.StatusObjectDao;
import com.epam.dlab.dto.PersistentStatusDto;
import com.epam.dlab.rest.client.RESTService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestoreResourceStatusServiceTest {

	@Mock
	private StatusObjectDao statusObjectDao;
	@Mock
	private RESTService selfService;
	@InjectMocks
	private RestoreResourceStatusService restoreResourceStatusService;

	@Test
	public void start() {
		final PersistentStatusDto dto1 = new PersistentStatusDto(null, "someUrl1");
		final PersistentStatusDto dto2 = new PersistentStatusDto(null, "someUrl2");
		when(statusObjectDao.findAll()).thenReturn(Arrays.asList(dto1, dto2));

		restoreResourceStatusService.start();

		verify(statusObjectDao).findAll();
		verifyNoMoreInteractions(statusObjectDao, selfService);
	}
}
