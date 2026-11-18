package com.pulsepass.notificationservice.api.messages;

import com.pulsepass.notificationservice.api.messages.definitions.EventData;
import com.pulsepass.notificationservice.application.dtos.input.ProcessNotificationEventDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationEventMessage(
        @NotNull NotificationEventMessageTypeEnum eventType,
        @NotBlank @Size(max = 36) String eventKey,
        @NotNull @Valid EventData eventData
) {

    public ProcessNotificationEventDTO toDTO() {
        return eventData.toDTO();
    }
}
