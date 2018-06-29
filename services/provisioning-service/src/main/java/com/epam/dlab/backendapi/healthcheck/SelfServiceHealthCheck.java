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
package com.epam.dlab.backendapi.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.epam.dlab.auth.SystemUserInfoService;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.google.inject.Inject;

import javax.ws.rs.core.Response;
import java.util.Objects;

public class SelfServiceHealthCheck extends HealthCheck {

	private UserInfo userInfo;

	@Inject
	private RESTService selfService;
	@Inject
	private SystemUserInfoService systemUserInfoService;

	@Override
	protected Result check() {
		if (Objects.isNull(userInfo)) {
			userInfo = systemUserInfoService.create("healthChecker");
		}
		final Response response =
				selfService.get(ApiCallbacks.INFRASTRUCTURE, userInfo.getAccessToken(), Response.class);
		return isSuccess(response) ? Result.healthy() : Result.unhealthy(response.getStatusInfo()
				.getReasonPhrase());
	}

	private boolean isSuccess(Response response) {
		return response.getStatus() == 200;
	}
}

