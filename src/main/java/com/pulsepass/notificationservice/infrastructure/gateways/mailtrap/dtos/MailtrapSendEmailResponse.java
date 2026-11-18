package com.pulsepass.notificationservice.infrastructure.gateways.mailtrap.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MailtrapSendEmailResponse(
        @JsonProperty("success") Boolean success,
        @JsonProperty("message_ids") List<String> messageIds,
        @JsonProperty("errors") List<String> errors
) {
}
