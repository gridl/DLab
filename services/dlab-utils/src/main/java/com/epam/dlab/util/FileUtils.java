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

package com.epam.dlab.util;

import com.epam.dlab.exceptions.DlabException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class FileUtils {

	private FileUtils() {
	}

	public static void saveToFile(String filename, String directory, String content) throws IOException {
		Path filePath = Paths.get(directory, filename).toAbsolutePath();
		log.debug("Saving content to {}", filePath.toString());
		try {
			com.google.common.io.Files.createParentDirs(new File(filePath.toString()));
		} catch (IOException e) {
			throw new DlabException("Can't create folder " + filePath + ": " + e.getLocalizedMessage(), e);
		}
		Files.write(filePath, content.getBytes());
	}

	public static void deleteFile(String filename, String directory) throws IOException {
		Path filePath = Paths.get(directory, filename).toAbsolutePath();
		log.debug("Deleting file from {}", filePath.toString());
		Files.deleteIfExists(filePath);
	}

	public static Optional<String> getIfExistsSimilar(String filename, String directory) {
		try (final Stream<String> pathStream = Files.lines(Paths.get(directory))) {
			return pathStream.filter(path -> path.contains(filename)).findAny();
		} catch (IOException e) {
			log.error("Problems occured with accessing directory {} due to: {}", directory, e.getLocalizedMessage());
		}
		return Optional.empty();
	}

	public static void copyFile(String sourcePath, String destPath) {
		try {
			log.debug("Copying file {} into directory {}...", sourcePath, destPath);
			Files.copy(Paths.get(sourcePath), Paths.get(destPath));
		} catch (IOException e) {
			log.error("Problems occured with copying file {} into directory {} due to: {}", sourcePath, destPath,
					e.getLocalizedMessage());
		}
	}
}
