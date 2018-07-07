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

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

public class DynamicApiAnnotation implements Api {

	private String value;
	private Authorization[] authorizations;
	private boolean hidden;

	DynamicApiAnnotation(String value, Authorization[] authorizations, boolean hidden) {
		this.value = value;
		this.authorizations = authorizations;
		this.hidden = hidden;
	}

	@Override
	public String value() {
		return value;
	}

	@Override
	public String[] tags() {
		return new String[0];
	}

	@Override
	public String description() {
		return StringUtils.EMPTY;
	}

	@Override
	public String basePath() {
		return StringUtils.EMPTY;
	}

	@Override
	public int position() {
		return 0;
	}

	@Override
	public String produces() {
		return StringUtils.EMPTY;
	}

	@Override
	public String consumes() {
		return StringUtils.EMPTY;
	}

	@Override
	public String protocols() {
		return StringUtils.EMPTY;
	}

	@Override
	public Authorization[] authorizations() {
		return authorizations;
	}

	@Override
	public boolean hidden() {
		return hidden;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}
}
