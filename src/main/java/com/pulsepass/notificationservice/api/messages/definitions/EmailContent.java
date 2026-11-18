package com.pulsepass.notificationservice.api.messages.definitions;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record EmailContent(@NotNull Map<String, Object> variables) {
}
