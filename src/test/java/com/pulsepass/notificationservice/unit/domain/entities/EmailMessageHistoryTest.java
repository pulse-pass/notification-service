package com.pulsepass.notificationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import org.junit.jupiter.api.Test;

class EmailMessageHistoryTest {

    @Test
    void fromEmailMessage_statusChange_buildsHistoryWithOldAndNewStatus() {
        var event = new NotificationEvent();
        event.setNotificationEventKey("event-key");
        var pending = new EmailMessageStatus(1L, "PENDING", null);
        var sent = new EmailMessageStatus(2L, "SENT", null);
        var message = new EmailMessage("message-key", event, "{}", sent);

        var history = EmailMessageHistory.fromEmailMessage(message, pending);

        assertThat(history.getEmailMessage()).isSameAs(message);
        assertThat(history.getOldStatus()).isSameAs(pending);
        assertThat(history.getNewStatus()).isSameAs(sent);
        assertThat(history.getEmailMessageHistoryKey()).hasSize(36);
        assertThat(history.getHistoryData()).isEqualTo(
                "{\"email_message_key\":\"message-key\",\"notification_event_key\":\"event-key\","
                        + "\"email_message_status\":\"SENT\"}");
    }

    @Test
    void fromEmailMessage_creation_hasNoOldStatus() {
        var event = new NotificationEvent();
        var message = new EmailMessage("message-key", event, "{}", new EmailMessageStatus(1L, "PENDING", null));

        var history = EmailMessageHistory.fromEmailMessage(message, null);

        assertThat(history.getOldStatus()).isNull();
    }
}
