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

package com.epam.dlab.process.serializer;

import com.epam.dlab.process.model.ProcessType;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProcessTypeSerializerTest {

	@Test
	public void serialize() throws IOException {
		com.fasterxml.jackson.core.JsonGenerator jsonGenerator = mock(com.fasterxml.jackson.core.JsonGenerator.class);
		SerializerProvider serializerProvider = mock(SerializerProvider.class);
		ProcessType processType = ProcessType.BACKUP_CREATE;

		new ProcessTypeSerializer().serialize(processType, jsonGenerator, serializerProvider);

		verify(jsonGenerator).writeString(ProcessType.BACKUP_CREATE.toString());
		verifyNoMoreInteractions(jsonGenerator);
		verifyZeroInteractions(serializerProvider);
	}
}