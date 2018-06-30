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
package com.epam.dlab.backendapi.service;

import com.epam.dlab.backendapi.dao.StatusObjectDao;
import com.epam.dlab.dto.PersistentStatusDto;
import com.epam.dlab.rest.client.RESTService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
@Slf4j
public class RestoreResourceStatusService implements Managed {

	private static final long TIMEOUT_SEC = 10L;

	@Inject
	private RESTService selfService;
	@Inject
	private StatusObjectDao statusObjectDao;

	@Override
	public void start() {
		List<PersistentStatusDto> objects = statusObjectDao.findAll();
		if (!objects.isEmpty()) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.execute(() -> {
				pause();
				repostAll(objects);
				removeAll(objects);
				log.info("Successfully reposted to self-service {} status dto objects.", objects.size());
			});
		}
	}

	@Override
	public void stop() {
		log.info("RestoreResourceStatusService stopped");
	}

	private void pause() {
		try {
			TimeUnit.SECONDS.sleep(TIMEOUT_SEC);
		} catch (InterruptedException e) {
			log.error("An exception occured: {}", e);
			Thread.currentThread().interrupt();
		}
	}

	private void repostAll(List<PersistentStatusDto> objects) {
		log.info("Reposting all status dto objects to self-service...");
		objects.forEach(persistentStatusDto ->
				selfService.post(persistentStatusDto.getCallbackUrl(), persistentStatusDto.getDto(), Response.class));
	}

	private void removeAll(List<PersistentStatusDto> objects) {
		log.debug("Removing all files with status dto objects data...");
		objects.forEach(object -> statusObjectDao.remove(object));
	}
}
