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

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.exceptions.DlabException;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.server.DefaultServerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class SelfServiceReplier implements Managed {

	private static final String EXCEPTION_MSG = "An exception occured: {}";
	private HttpClient client = HttpClientBuilder.create().build();
	private final ObjectMapper mapper = new ObjectMapper();

	@Inject
	private ProvisioningServiceApplicationConfiguration configuration;

	@Override
	public void start() {
		log.debug("Waiting for self-service...");
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			boolean isAvailable;
			do {
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e) {
					log.error(EXCEPTION_MSG, e);
					Thread.currentThread().interrupt();
				}
				isAvailable = isSelfServiceAlive();
			} while (!isAvailable);
			log.info("Self-service is available now. Saved data are sending...");
		});
		executorService.shutdown();
	}

	@Override
	public void stop() {
		log.info("SelfServiceReplier stopped.");
	}

	private String getHealthCheckUrl() {
		ConnectorFactory connectorFactory =
				((DefaultServerFactory) configuration.getServerFactory()).getAdminConnectors().stream().findAny()
						.orElseThrow(() -> new DlabException("Admin connector is not defined in current configuration" +
								"."));
		Class<? extends ConnectorFactory> clazz = connectorFactory.getClass();
		String type = clazz.isAnnotationPresent(JsonTypeName.class) ?
				clazz.getAnnotation(JsonTypeName.class).value() : clazz.getName();
		int port;
		Optional<String> host;
		if (type.equals("https") || type.equals("http")) {
			HttpConnectorFactory httpConnectorFactory = (HttpConnectorFactory) connectorFactory;
			port = httpConnectorFactory.getPort();
			host = Optional.ofNullable(httpConnectorFactory.getBindHost());
		} else throw new DlabException("Unknown type of connector factory: " + type);
		return String.format("%s://%s:%s/healthcheck", type, host.orElse("localhost"), port);
	}

	private boolean isSelfServiceAlive() {
		final String healthCheckUrl = getHealthCheckUrl();
		HttpGet request = new HttpGet(healthCheckUrl);
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (IOException e) {
			log.error(EXCEPTION_MSG, e);
			throw new DlabException("Couldn't execute request on URL: " + healthCheckUrl);
		}
		JsonNode json;
		try {
			json = mapper.readTree(response.getEntity().getContent());
		} catch (IOException e) {
			log.error(EXCEPTION_MSG, e);
			throw new DlabException("Couldn't obtain content from response: " + response.toString());
		}
		return json.get(ServiceConsts.SELF_SERVICE_NAME).get("healthy").asBoolean();
	}
}
