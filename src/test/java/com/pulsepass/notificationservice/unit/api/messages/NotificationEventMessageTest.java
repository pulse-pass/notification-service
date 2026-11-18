package com.pulsepass.notificationservice.unit.api.messages;

import static com.pulsepass.notificationservice.api.messages.NotificationEventMessageTypeEnum.NOTIFICATION_EVENT_CREATION;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.api.messages.NotificationEventMessage;
import com.pulsepass.notificationservice.api.messages.NotificationEventMessageTypeEnum;
import com.pulsepass.notificationservice.api.messages.definitions.EmailContent;
import com.pulsepass.notificationservice.api.messages.definitions.EventData;
import com.pulsepass.notificationservice.api.messages.definitions.ReceiverContact;
import com.pulsepass.notificationservice.common.JsonParseHandlers;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationEventMessageTest {

    private static final String VALID_JSON = """
            {
              "event_type": "NOTIFICATION_EVENT_CREATION",
              "event_key": "8c9f6d52-2a5e-4a31-9e5b-0b8f6f8f1a11",
              "event_data": {
                "notification_event_key": "8c9f6d52-2a5e-4a31-9e5b-0b8f6f8f1a11",
                "notification_event_type": "ORGANIZER_APPROVED",
                "person_key": "person-key",
                "origin": "ORGANIZATION",
                "origin_key": "origin-key",
                "receiver_contact": {"name": "Maria Silva", "email": "maria@empresa.com"},
                "email": {"variables": {"legal_name": "Eventos Silva LTDA"}}
              }
            }
            """;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private NotificationEventMessage message(
            NotificationEventMessageTypeEnum eventType, String eventKey, EventData eventData) {
        return new NotificationEventMessage(eventType, eventKey, eventData);
    }

    private EventData eventData() {
        return new EventData(
                "event-key", "ORGANIZER_APPROVED", "person-key", "ORGANIZATION", "origin-key",
                new ReceiverContact("Maria Silva", "maria@empresa.com"),
                new EmailContent(Map.of("legal_name", "Eventos Silva LTDA")));
    }

    @Test
    void validate_validJson_hasNoViolations() {
        var parsed = JsonParseHandlers.fromJson(VALID_JSON, NotificationEventMessage.class);

        assertThat(validator.validate(parsed)).isEmpty();
        assertThat(parsed.eventType()).isEqualTo(NOTIFICATION_EVENT_CREATION);
        assertThat(parsed.eventData().notificationEventType()).isEqualTo("ORGANIZER_APPROVED");
        assertThat(parsed.eventData().receiverContact().email()).isEqualTo("maria@empresa.com");
        assertThat(parsed.eventData().email().variables()).containsEntry("legal_name", "Eventos Silva LTDA");
    }

    @Test
    void validate_nullEventType_hasViolation() {
        assertThat(validator.validate(message(null, "key", eventData()))).hasSize(1);
    }

    @Test
    void validate_blankEventKey_hasViolation() {
        assertThat(validator.validate(message(NOTIFICATION_EVENT_CREATION, "", eventData()))).hasSize(1);
    }

    @Test
    void validate_missingEventData_hasViolation() {
        assertThat(validator.validate(message(NOTIFICATION_EVENT_CREATION, "key", null))).hasSize(1);
    }

    @Test
    void validate_invalidNestedReceiverEmail_hasViolation() {
        var data = new EventData(
                "key", "ORGANIZER_APPROVED", null, "ORGANIZATION", "origin-key",
                new ReceiverContact("Maria", "not-an-email"),
                new EmailContent(Map.of()));

        assertThat(validator.validate(message(NOTIFICATION_EVENT_CREATION, "key", data))).hasSize(1);
    }

    @Test
    void toDTO_validMessage_delegatesToEventData() {
        var data = eventData();

        assertThat(message(NOTIFICATION_EVENT_CREATION, "other-key", data).toDTO()).isEqualTo(data.toDTO());
    }

    @Test
    void toDTO_validMessage_usesNotificationEventFieldsOfEventData() {
        var dto = message(NOTIFICATION_EVENT_CREATION, "other-key", eventData()).toDTO();

        assertThat(dto.notificationEventKey()).isEqualTo("event-key");
        assertThat(dto.notificationEventType()).isEqualTo("ORGANIZER_APPROVED");
    }
}
