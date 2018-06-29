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
import com.epam.dlab.dto.StatusBaseDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Singleton
public class SelfServiceHelper<T extends StatusBaseDTO<?>> {

	private String healthCheckUrl;

	@Inject
	private ProvisioningServiceApplicationConfiguration configuration;
	@Inject
	private RESTService selfService;
	@Inject
	private ObjectMapper mapper;

	public boolean isSelfServiceAlive() {
		if (Objects.isNull(healthCheckUrl)) {
			healthCheckUrl = getHealthCheckUrl();
		}
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(healthCheckUrl);
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (IOException e) {
			log.error("An exception occured: {}", e);
			throw new DlabException("Couldn't execute request on URL: " + healthCheckUrl);
		}
		JsonNode json;
		try {
			json = mapper.readTree(response.getEntity().getContent());
		} catch (IOException e) {
			log.error("An exception occured: {}", e);
			throw new DlabException("Couldn't obtain content from response: " + response.toString());
		}
		return json.get(ServiceConsts.SELF_SERVICE_NAME).get("healthy").asBoolean();
	}

	public void post(String url, String uuid, T object) {
		log.debug("Sending post request to self service {} for UUID {}, object is {}", url, uuid, object);
		try {
			selfService.post(url, object, Response.class);
		} catch (Exception e) {
			log.error("Send request or response error for UUID {}: {}", uuid, e.getLocalizedMessage(), e);
			throw new DlabException("Send request or responce error for UUID " + uuid + ": " +
					e.getLocalizedMessage(), e);
		}
	}

	private String getHealthCheckUrl() {
		ConnectorFactory connectorFactory = ((DefaultServerFactory) configuration.getServerFactory())
				.getAdminConnectors().stream().findAny().orElseThrow(() ->
						new DlabException("Admin connector is not defined in current configuration."));
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
		return String.format("%s://%s:%d/healthcheck", type, host.orElse("localhost"), port);
	}
}
