package com.pulsepass.notificationservice.integration;

import com.pulsepass.notificationservice.common.KafkaTools;
import com.pulsepass.notificationservice.common.MockTools;
import org.junit.jupiter.api.BeforeAll;
import tools.jackson.databind.json.JsonMapper;

public abstract class BaseIntegrationTest {

    protected static final String MAILTRAP_SANDBOX_ID = env("MAILTRAP_SANDBOX_ID", "1234567");

    protected final JsonMapper jsonMapper = JsonMapper.builder().build();

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @BeforeAll
    static void setup() {
        MockTools.configure(
                env("MOCKSERVER_BASE_URL", "http://localhost"),
                Integer.parseInt(env("MOCKSERVER_PORT", "1083"))
        );
        KafkaTools.configure(env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9095"));
    }
}
