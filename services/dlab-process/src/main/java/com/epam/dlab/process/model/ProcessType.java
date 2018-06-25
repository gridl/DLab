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
package com.epam.dlab.process.model;

public enum ProcessType {

	BACKUP_CREATE("Backup_creating"),
	GIT_CREDS_UPDATE("Git_creds_updating"),
	IMAGE_CREATE("Image_creating"),
	FETCH_RESOURCES_STATUS("Resources_status_fetching"),
	FETCH_DOCKER_IMAGES("Docker_images_fetching"),
	FETCH_DOCKER_RUNNING_CONTAINERS("Docker_running_containers_fetching"),
	DOCKER_IMAGE_RUN("Docker_image_running"),
	DOCKER_IMAGE_WARM_UP("Docker_image_warming_up"),
	FETCH_EXPLORATORY_LIB_LIST("Fetching_exploratory_lib_list"),
	FETCH_CLUSTER_LIB_LIST("Fetching_cluster_lib_list"),
	EXPLORATORY_LIBRARY_INSTALL("Exploratory_library_installing"),
	CLUSTER_LIBRARY_INSTALL("Cluster_library_installing"),
	REUPLOAD_KEY("Key_reuploading"),

	EDGE_CREATE("Edge_creating"),
	EDGE_START("Edge_starting"),
	EDGE_STOP("Edge_stopping"),
	EDGE_TERMINATE("Edge_terminating"),

	EXPLORATORY_CREATE("Exploratory_creating"),
	EXPLORATORY_START("Exploratory_starting"),
	EXPLORATORY_STOP("Exploratory_stopping"),
	EXPLORATORY_TERMINATE("Exploratory_terminating"),

	SPARK_CLUSTER_CREATE("Spark_cluster_creating"),
	SPARK_CLUSTER_CONFIGURE("Spark_cluster_configuring"),
	SPARK_CLUSTER_START("Spark_cluster_starting"),
	SPARK_CLUSTER_STOP("Spark_cluster_stopping"),
	SPARK_CLUSTER_TERMINATE("Spark_cluster_terminating"),

	DATAENGINE_SERVICE_CREATE("DES_cluster_creating"),
	DATAENGINE_SERVICE_CONFIGURE("DES_cluster_configuring"),
	DATAENGINE_SERVICE_TERMINATE("DES_cluster_terminating"),

	MOCKED_PROCESS("Mocked_process");


	private String name;

	ProcessType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
