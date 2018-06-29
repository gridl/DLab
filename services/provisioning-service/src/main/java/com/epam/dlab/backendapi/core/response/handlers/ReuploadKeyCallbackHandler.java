package com.epam.dlab.backendapi.core.response.handlers;

import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.service.SelfServiceHelper;
import com.epam.dlab.dto.reuploadkey.ReuploadKeyCallbackDTO;
import com.epam.dlab.dto.reuploadkey.ReuploadKeyStatus;
import com.epam.dlab.dto.reuploadkey.ReuploadKeyStatusDTO;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReuploadKeyCallbackHandler implements FileHandlerCallback {
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
	private static final String STATUS_FIELD = "status";
	private static final String ERROR_MESSAGE_FIELD = "error_message";
	@JsonProperty
	private final String uuid;
	@JsonProperty
	private final ReuploadKeyCallbackDTO dto;
	private final SelfServiceHelper selfServiceHelper;
	@JsonProperty
	private final String callbackUrl;
	@JsonProperty
	private final String user;

	@JsonCreator
	public ReuploadKeyCallbackHandler(@JacksonInject SelfServiceHelper selfServiceHelper,
									  @JsonProperty("callbackUrl") String callbackUrl,
									  @JsonProperty("user") String user,
									  @JsonProperty("dto") ReuploadKeyCallbackDTO dto) {
		this.selfServiceHelper = selfServiceHelper;
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
		log.debug("Got file {} while waiting for UUID {}, reupload key response: {}", fileName, uuid, fileContent);

		final JsonNode jsonNode = MAPPER.readTree(fileContent);
		final String status = jsonNode.get(STATUS_FIELD).textValue();
		ReuploadKeyStatusDTO reuploadKeyStatusDTO;
		if ("ok".equals(status)) {
			reuploadKeyStatusDTO = buildReuploadKeyStatusDto(ReuploadKeyStatus.COMPLETED);
		} else {
			reuploadKeyStatusDTO = buildReuploadKeyStatusDto(ReuploadKeyStatus.FAILED)
					.withErrorMessage(jsonNode.get(ERROR_MESSAGE_FIELD).textValue());
		}
		if (selfServiceHelper.isSelfServiceAlive()) {
			selfServiceHelper.post(callbackUrl, uuid, reuploadKeyStatusDTO);
		}
		return "ok".equals(status);
	}

	@Override
	public void handleError(String errorMessage) {
		buildReuploadKeyStatusDto(ReuploadKeyStatus.FAILED)
				.withErrorMessage(errorMessage);
	}

	private ReuploadKeyStatusDTO buildReuploadKeyStatusDto(ReuploadKeyStatus status) {
		return new ReuploadKeyStatusDTO()
				.withRequestId(uuid)
				.withReuploadKeyCallbackDto(dto)
				.withReuploadKeyStatus(status)
				.withUser(user);
	}

}

