package com.pulsepass.notificationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationEventTypeTest {

    @Test
    void getEmailVariableNames_commaSeparated_returnsEachName() {
        var type = new NotificationEventType(
                1L, "ORGANIZER_REJECTED", "subject", "template",
                "legal_name,rejection_reason_description", LocalDateTime.now());

        assertThat(type.getEmailVariableNames()).containsExactly("legal_name", "rejection_reason_description");
    }

    @Test
    void getEmailVariableNames_singleName_returnsOneItem() {
        var type = new NotificationEventType();
        type.setEmailVariables("legal_name");

        assertThat(type.getEmailVariableNames()).containsExactly("legal_name");
    }
}
