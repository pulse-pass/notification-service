package com.pulsepass.notificationservice.infrastructure.gateways.mailtrap.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MailtrapSendEmailRequest(
        @JsonProperty("from") Address from,
        @JsonProperty("to") List<Address> to,
        @JsonProperty("subject") String subject,
        @JsonProperty("text") String text,
        @JsonProperty("html") String html,
        @JsonProperty("category") String category
) {

    public record Address(
            @JsonProperty("email") String email,
            @JsonProperty("name") String name
    ) {
    }
}
