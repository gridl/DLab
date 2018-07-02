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

package com.epam.dlab.backendapi.core.response.handlers;

import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.service.InfrastructureCallbackHandlerService;
import com.epam.dlab.backendapi.service.SelfServiceHelper;
import com.epam.dlab.dto.PersistentStatusDto;
import com.epam.dlab.dto.backup.EnvBackupDTO;
import com.epam.dlab.dto.backup.EnvBackupStatus;
import com.epam.dlab.dto.backup.EnvBackupStatusDTO;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BackupCallbackHandler implements FileHandlerCallback {
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
	private static final String STATUS_FIELD = "status";
	private static final String BACKUP_FILE_FIELD = "backup_file";
	private static final String ERROR_MESSAGE_FIELD = "error_message";
	@JsonProperty
	private final String uuid;
	@JsonProperty
	private final EnvBackupDTO dto;
	private final SelfServiceHelper selfServiceHelper;
	private final InfrastructureCallbackHandlerService infrastructureCallbackHandlerService;
	@JsonProperty
	private final String callbackUrl;
	@JsonProperty
	private final String user;

	@JsonCreator
	public BackupCallbackHandler(@JacksonInject SelfServiceHelper selfServiceHelper,
								 @JacksonInject InfrastructureCallbackHandlerService
										 infrastructureCallbackHandlerService,
								 @JsonProperty("callbackUrl") String callbackUrl, @JsonProperty("user") String user,
								 @JsonProperty("dto") EnvBackupDTO dto) {
		this.selfServiceHelper = selfServiceHelper;
		this.infrastructureCallbackHandlerService = infrastructureCallbackHandlerService;
		this.uuid = dto.getId();
		this.callbackUrl = callbackUrl;
		this.user = user;
		this.dto = dto;
	}

	@Override
	public String getUUID() {
		return uuid;
	}

	@Override
	public boolean checkUUID(String uuid) {
		return this.uuid.equals(uuid);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean handle(String fileName, byte[] content) throws Exception {
		final String fileContent = new String(content);
		log.debug("Got file {} while waiting for UUID {}, backup response: {}", fileName, uuid, fileContent);

		final JsonNode jsonNode = MAPPER.readTree(fileContent);
		final EnvBackupStatus status = EnvBackupStatus.fromValue(jsonNode.get(STATUS_FIELD).textValue());
		EnvBackupStatusDTO envBackupStatusDTO;
		if (EnvBackupStatus.CREATED == status) {
			envBackupStatusDTO = buildBackupStatusDto(EnvBackupStatus.CREATED)
					.withBackupFile(jsonNode.get(BACKUP_FILE_FIELD).textValue());
		} else {
			envBackupStatusDTO = buildBackupStatusDto(EnvBackupStatus.FAILED)
					.withErrorMessage(jsonNode.get(ERROR_MESSAGE_FIELD).textValue());
		}
		if (selfServiceHelper.isSelfServiceAlive()) {
			selfServiceHelper.post(callbackUrl, uuid, envBackupStatusDTO);
		} else {
			infrastructureCallbackHandlerService.save(new PersistentStatusDto(envBackupStatusDTO, callbackUrl));
		}
		return EnvBackupStatus.CREATED == status;
	}

	@Override
	public void handleError(String errorMessage) {
		buildBackupStatusDto(EnvBackupStatus.FAILED)
				.withErrorMessage(errorMessage);
	}

	protected EnvBackupStatusDTO buildBackupStatusDto(EnvBackupStatus status) {
		return new EnvBackupStatusDTO()
				.withRequestId(uuid)
				.withEnvBackupDTO(dto)
				.withStatus(status)
				.withUser(user);
	}

}
