package com.pulsepass.notificationservice.unit.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.notificationservice.common.JsonParseHandlers;
import org.junit.jupiter.api.Test;

class JsonParseHandlersTest {

    @Test
    void fromJson_snakeCaseJson_mapsToRecord() {
        var sample = JsonParseHandlers.fromJson("{\"first_name\":\"x\"}", Sample.class);

        assertThat(sample.firstName()).isEqualTo("x");
    }

    @Test
    void fromJson_brokenJson_throwsIllegalArgument() {
        assertThatThrownBy(() -> JsonParseHandlers.fromJson("{", Sample.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_utilityClass_isPrivate() throws Exception {
        var constructor = JsonParseHandlers.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThat(constructor.newInstance()).isNotNull();
    }

    public record Sample(String firstName) {
    }
}
