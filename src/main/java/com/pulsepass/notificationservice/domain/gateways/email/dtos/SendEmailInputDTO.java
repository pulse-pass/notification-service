package com.pulsepass.notificationservice.domain.gateways.email.dtos;

import java.util.Map;

public record SendEmailInputDTO(
        String recipientEmail,
        String recipientName,
        String subject,
        String template,
        Map<String, Object> variables
) {
}
