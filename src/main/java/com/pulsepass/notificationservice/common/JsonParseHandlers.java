package com.pulsepass.notificationservice.common;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

public final class JsonParseHandlers {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    private JsonParseHandlers() {
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return JSON_MAPPER.readValue(json, type);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to parse JSON", e);
        }
    }
}
