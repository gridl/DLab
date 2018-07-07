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
import com.epam.dlab.backendapi.resources.aws.ComputationalResourceAws;
import com.epam.dlab.backendapi.resources.azure.ComputationalResourceAzure;
import com.epam.dlab.backendapi.resources.gcp.ComputationalResourceGcp;
import com.epam.dlab.cloud.CloudProvider;
import com.epam.dlab.exceptions.DlabException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Singleton
public class SwaggerAnnotationResolver implements Managed {

	private static final String ANNOTATION_METHOD = "annotationData";
	private static final String ANNOTATIONS = "annotations";

	@Inject
	private SelfServiceApplicationConfiguration configuration;

	@Override
	public void start() {
		log.debug("SwaggerAnnotationResolver started");
		setResourceClassVisibleForSwagger(configuration.getCloudProvider());
	}

	@Override
	public void stop() {
		log.debug("SwaggerAnnotationResolver stopped");
	}

	private void setResourceClassVisibleForSwagger(CloudProvider cloudProvider) {
		Class<?> clazz = getTargetClass(cloudProvider);
		Api apiAnnotation = clazz.getAnnotation(Api.class);
		Api targetValue = new DynamicApiAnnotation(apiAnnotation.value(), apiAnnotation.authorizations(), false);
		alterAnnotationValue(clazz, Api.class, targetValue);
	}

	private Class<?> getTargetClass(CloudProvider cloudProvider) {
		if (cloudProvider == CloudProvider.AWS) {
			return ComputationalResourceAws.class;
		} else if (cloudProvider == CloudProvider.AZURE) {
			return ComputationalResourceAzure.class;
		} else if (cloudProvider == CloudProvider.GCP) {
			return ComputationalResourceGcp.class;
		} else {
			throw new DlabException("Invalid cloud provider");
		}
	}

	@SuppressWarnings("unchecked")
	private void alterAnnotationValue(Class<?> targetClass, Class<? extends Annotation> targetAnnotation,
									  Annotation targetValue) {
		try {
			Method method = Class.class.getDeclaredMethod(ANNOTATION_METHOD);
			method.setAccessible(true);

			Object annotationData = method.invoke(targetClass);

			Field annotations = annotationData.getClass().getDeclaredField(ANNOTATIONS);
			annotations.setAccessible(true);

			Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>)
					annotations.get(annotationData);
			map.put(targetAnnotation, targetValue);
		} catch (Exception e) {
			log.error("An exception occured: {}", e.getLocalizedMessage());
		}
	}
}
