package com.pulsepass.notificationservice.api.messages.definitions;

import com.pulsepass.notificationservice.application.dtos.input.ProcessNotificationEventDTO;
import com.pulsepass.notificationservice.common.JsonHandlers;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventData(
        @NotBlank @Size(max = 36) String notificationEventKey,
        @NotBlank @Size(max = 50) String notificationEventType,
        @Size(max = 36) String personKey,
        @NotBlank @Size(max = 50) String origin,
        @NotBlank @Size(max = 36) String originKey,
        @NotNull @Valid ReceiverContact receiverContact,
        @NotNull @Valid EmailContent email
) {

    public ProcessNotificationEventDTO toDTO() {
        return new ProcessNotificationEventDTO(
                notificationEventType,
                notificationEventKey,
                personKey,
                origin,
                originKey,
                receiverContact.name(),
                receiverContact.email(),
                email.variables(),
                JsonHandlers.toJson(this)
        );
    }
}
