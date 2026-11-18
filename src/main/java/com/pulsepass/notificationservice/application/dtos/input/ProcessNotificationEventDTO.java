package com.pulsepass.notificationservice.application.dtos.input;

import java.util.Map;

public record ProcessNotificationEventDTO(
        String notificationEventType,
        String notificationEventKey,
        String personKey,
        String origin,
        String originKey,
        String receiverName,
        String receiverEmail,
        Map<String, Object> emailVariables,
        String data
) {
}
