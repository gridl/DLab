/***************************************************************************

 Copyright (c) 2016, EPAM SYSTEMS INC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ****************************************************************************/

package com.epam.dlab.rest.mappers;

import com.epam.dlab.rest.dto.ErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public abstract class GenericExceptionMapper<E extends Exception> implements ExceptionMapper<E> {
	static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionMapper.class);

	@Override
	public Response toResponse(E exception) {
		LOGGER.error("Uncaught exception in application", exception);

		return Response
				.serverError()
				.entity(new ErrorDTO(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getMessage()))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
}
