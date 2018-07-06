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
package com.epam.dlab.backendapi.swagger;

import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

public class SwaggerConfigurator extends SwaggerBundle<SelfServiceApplicationConfiguration> {

	private static SwaggerConfigurator instance;
	private Swagger swagger;
	public static final String BASIC_AUTH = "Basic authorization";
	public static final String TOKEN_AUTH = "Token authorization";


	private SwaggerConfigurator() {
		swagger = new Swagger();
		configureSwagger();
		new SwaggerContextService().updateSwagger(swagger);
	}

	@Override
	protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(SelfServiceApplicationConfiguration
																			   configuration) {
		SwaggerBundleConfiguration swaggerConfiguration = new SwaggerBundleConfiguration();
		swaggerConfiguration.setResourcePackage(configuration.getSwaggerResourcePackages());
		swaggerConfiguration.setTitle("DLab API");
		swaggerConfiguration.setDescription("Essential toolset for analytics. Deployed on " +
				configuration.getCloudProvider().getName().toUpperCase());
		swaggerConfiguration.setVersion("2.0");
		swaggerConfiguration.setContact("DLab");
		swaggerConfiguration.setContactUrl("http://dlab.opensource.epam.com/");
		swaggerConfiguration.setLicense("Apache 2.0");
		swaggerConfiguration.setLicenseUrl("https://www.apache.org/licenses/LICENSE-2.0");
		return swaggerConfiguration;
	}

	public static SwaggerConfigurator getInstance() {
		if (instance == null) {
			instance = new SwaggerConfigurator();
		}
		return instance;
	}

	private void configureSwagger() {
		swagger.consumes(MediaType.APPLICATION_JSON).produces(MediaType.APPLICATION_JSON)
				.schemes(Arrays.asList(Scheme.HTTPS, Scheme.HTTP))
				.externalDocs(new io.swagger.models.ExternalDocs().description("GitHub")
						.url("https://github.com/epam/DLab"))
				.securityDefinition(TOKEN_AUTH,
						new io.swagger.models.auth.ApiKeyAuthDefinition("Authorization", In.HEADER))
				.securityDefinition(BASIC_AUTH, new BasicAuthDefinition());
	}
}
