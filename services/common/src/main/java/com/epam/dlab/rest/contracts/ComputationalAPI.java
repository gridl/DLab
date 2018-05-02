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

package com.epam.dlab.rest.contracts;

public class ComputationalAPI {
	public static final String LIBRARY = "library/";
	public static final String COMPUTATIONAL = "computational";
	private static final String COMPUTATIONAL_CREATE = COMPUTATIONAL + "/create";
	private static final String COMPUTATIONAL_STOP = COMPUTATIONAL + "/stop";
	private static final String COMPUTATIONAL_START = COMPUTATIONAL + "/start";
	private static final String SPARK = "/spark";
	public static final String COMPUTATIONAL_CREATE_SPARK = COMPUTATIONAL_CREATE + SPARK;
	public static final String COMPUTATIONAL_CREATE_CLOUD_SPECIFIC = COMPUTATIONAL_CREATE + "/cloud";
	private static final String COMPUTATIONAL_TERMINATE = COMPUTATIONAL + "/terminate";
	public static final String COMPUTATIONAL_TERMINATE_SPARK = COMPUTATIONAL_TERMINATE + SPARK;
	public static final String COMPUTATIONAL_STOP_SPARK = COMPUTATIONAL_STOP + SPARK;
	public static final String COMPUTATIONAL_START_SPARK = COMPUTATIONAL_START + SPARK;
	public static final String COMPUTATIONAL_TERMINATE_CLOUD_SPECIFIC = COMPUTATIONAL_TERMINATE + "/cloud";
	public static final String COMPUTATIONAL_LIB_INSTALL = LIBRARY + COMPUTATIONAL + "/lib_install";
	public static final String COMPUTATIONAL_LIB_LIST = LIBRARY + COMPUTATIONAL + "/lib_list";

	private ComputationalAPI() {
	}
}
