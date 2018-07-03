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
package com.epam.dlab.backendapi.dao;


import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.dto.PersistentStatusDto;
import com.epam.dlab.dto.reuploadkey.ReuploadKeyStatusDTO;
import com.epam.dlab.exceptions.DlabException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class FileSystemStatusObjectDaoTest {

	@Mock
	private ObjectMapper mapper;
	@Mock
	private SelfServiceApplicationConfiguration configuration;
	@InjectMocks
	private FileSystemStatusObjectDao fileSystemStatusObjectDao;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void createHandlersFolder() throws IOException {
		folder.newFolder("opt", "dto");
	}

	@Test
	public void findAll() throws IOException {
		final File dto1 = getDtoFile("test1.json");
		final File dto2 = getDtoFile("test2.json");
		final String dtoFolder = getDtoFolder();

		when(configuration.getStatusDtoDirectory()).thenReturn(dtoFolder);
		final PersistentStatusDto persistentStatusDto1 = new PersistentStatusDto(null, "someUrl1");
		final PersistentStatusDto persistentStatusDto2 = new PersistentStatusDto(null, "someUrl2");
		when(mapper.readValue(any(File.class), Matchers.<Class<PersistentStatusDto>>any())).thenReturn
				(persistentStatusDto1).thenReturn(persistentStatusDto2);
		final List<PersistentStatusDto> dtos = fileSystemStatusObjectDao.findAll();

		assertEquals(2, dtos.size());

		verify(configuration).getStatusDtoDirectory();
		verify(mapper).readValue(dto1, PersistentStatusDto.class);
		verify(mapper).readValue(dto2, PersistentStatusDto.class);
		verifyNoMoreInteractions(mapper, configuration);
	}

	@Test
	public void findAllWithException() throws IOException {
		new File(getDtoFolder()).delete();
		when(configuration.getStatusDtoDirectory()).thenReturn(getDtoFolder());
		when(mapper.readValue(any(File.class), Matchers.<Class<PersistentStatusDto>>any()))
				.thenThrow(new RuntimeException("Exception"));
		final List<PersistentStatusDto> handlers = fileSystemStatusObjectDao.findAll();

		assertEquals(0, handlers.size());

		verify(configuration).getStatusDtoDirectory();
		verifyNoMoreInteractions(mapper, configuration);
	}

	@Test
	public void findAllWithOneWrongDtoFile() throws IOException {
		final File dto1 = getDtoFile("test1.json");
		final File dto2 = getDtoFile("test2.json");
		final String dtoFolder = getDtoFolder();

		final PersistentStatusDto persistentStatusDto1 = new PersistentStatusDto(null, "someUrl");

		when(configuration.getStatusDtoDirectory()).thenReturn(dtoFolder);
		when(mapper.readValue(any(File.class), Matchers.<Class<PersistentStatusDto>>any()))
				.thenReturn(persistentStatusDto1).thenThrow(new RuntimeException("Exception"));

		final List<PersistentStatusDto> dtos = fileSystemStatusObjectDao.findAll();

		assertEquals(1, dtos.size());

		verify(configuration).getStatusDtoDirectory();
		verify(mapper).readValue(dto1, PersistentStatusDto.class);
		verify(mapper).readValue(dto2, PersistentStatusDto.class);
		verifyNoMoreInteractions(mapper, configuration);
	}

	@Test
	public void remove() throws IOException {
		final File dto = getDtoFile("ReuploadKeyStatusDTO_someUuid.json");
		dto.createNewFile();
		final String dtoFolder = getDtoFolder();
		final ReuploadKeyStatusDTO statusDTO = new ReuploadKeyStatusDTO();
		statusDTO.withRequestId("someUuid");

		when(configuration.getStatusDtoDirectory()).thenReturn(dtoFolder);
		fileSystemStatusObjectDao.remove(new PersistentStatusDto(statusDTO, "someUrl"));

		assertFalse(dto.exists());

		verify(configuration).getStatusDtoDirectory();
		verifyNoMoreInteractions(configuration, mapper);
	}

	@Test
	public void removeWithException() {
		final String dtoFolder = getDtoFolder();
		final ReuploadKeyStatusDTO statusDTO = new ReuploadKeyStatusDTO();
		statusDTO.withRequestId("someUuid");

		when(configuration.getStatusDtoDirectory()).thenReturn(dtoFolder);
		expectedException.expect(DlabException.class);
		expectedException.expectMessage("Can not remove file ReuploadKeyStatusDTO_someUuid.json due to");
		fileSystemStatusObjectDao.remove(new PersistentStatusDto(statusDTO, "someUrl"));
	}

	private String getDtoFolder() {
		return folder.getRoot().getAbsolutePath() + File.separator + "opt" + File.separator + "dto";
	}

	private File getDtoFile(String dtoFileName) throws IOException {
		return folder.newFile(File.separator + "opt" + File.separator + "dto" + File.separator + dtoFileName);
	}
}
