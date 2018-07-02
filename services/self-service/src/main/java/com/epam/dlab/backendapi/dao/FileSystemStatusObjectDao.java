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
import com.epam.dlab.exceptions.DlabException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Singleton
@Slf4j
public class FileSystemStatusObjectDao implements StatusObjectDao {

	@Inject
	private SelfServiceApplicationConfiguration configuration;
	@Inject
	private ObjectMapper mapper;

	@Override
	public List<PersistentStatusDto> findAll() {
		try (final Stream<Path> pathStream = Files.list(Paths.get(configuration.getStatusDtoDirectory()))) {
			return pathStream.map(this::toPersistentStatusDto)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(toList());
		} catch (IOException e) {
			log.error("Can not restore status dto objects due to: {}", e.getMessage(), e);
		}
		return Collections.emptyList();
	}

	@Override
	public void remove(PersistentStatusDto object) {
		String filename = object.getStatusDto().getClass().getSimpleName() + "_" + object.getStatusDto().getRequestId()
				+ ".json";
		try {
			Files.delete(Paths.get(getAbsolutePath(filename)));
		} catch (Exception e) {
			log.error("Can not remove file {} with status dto object data due to: {}", filename, e.getMessage());
			throw new DlabException("Can not remove file " + filename + " due to: " + e.getMessage());
		}
	}

	private Optional<PersistentStatusDto> toPersistentStatusDto(Path path) {
		try {
			return Optional.of(mapper.readValue(path.toFile(), PersistentStatusDto.class));
		} catch (Exception e) {
			log.warn("Can not deserialize status dto object from file: {}", path.toString());
		}
		return Optional.empty();
	}

	private String getAbsolutePath(String fileName) {
		return configuration.getStatusDtoDirectory() + File.separator + fileName;
	}

}
