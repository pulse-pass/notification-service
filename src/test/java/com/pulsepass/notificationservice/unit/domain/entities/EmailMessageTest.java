package com.pulsepass.notificationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import org.junit.jupiter.api.Test;

class EmailMessageTest {

    @Test
    void toHistoryData_validMessage_returnsKeysAndStatusWithoutEmailData() {
        var event = new NotificationEvent();
        event.setNotificationEventKey("event-key");
        var status = new EmailMessageStatus(2L, "SENT", null);
        var message = new EmailMessage("message-key", event, "{\"recipient_email\":\"a@b.com\"}", status);

        var data = message.toHistoryData();

        assertThat(data).containsOnlyKeys("email_message_key", "notification_event_key", "email_message_status");
        assertThat(data.get("email_message_key")).isEqualTo("message-key");
        assertThat(data.get("notification_event_key")).isEqualTo("event-key");
        assertThat(data.get("email_message_status")).isEqualTo("SENT");
    }
}
