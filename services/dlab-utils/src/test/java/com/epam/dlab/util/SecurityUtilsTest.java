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
package com.epam.dlab.util;

import org.junit.Assert;
import org.junit.Test;

public class SecurityUtilsTest {

	@Test
	public void hideCredsTest() {
		String[] strings = {"bash", "-c", "\"edge_user_name\":\"edgeUserName\",\"conf_service_base_name\":\"SBN\", " +
				"\"password\":\"12345\""};
		String actual = SecurityUtils.hideCreds(strings);
		String expected = "bash -c \"edge_user_name\":\"edgeUserName\",\"conf_service_base_name\":\"SBN\", " +
				"\"password\":\"***\" ";
		Assert.assertEquals(expected, actual);
	}
}
