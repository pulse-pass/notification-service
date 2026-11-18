package com.pulsepass.notificationservice.unit.api.messages.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.api.messages.definitions.EmailContent;
import com.pulsepass.notificationservice.api.messages.definitions.EventData;
import com.pulsepass.notificationservice.api.messages.definitions.ReceiverContact;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EventDataTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ReceiverContact receiver = new ReceiverContact("Maria", "maria@empresa.com");
    private final EmailContent email = new EmailContent(Map.of("legal_name", "x"));

    private EventData dataOf(
            String personKey, String origin, String originKey, ReceiverContact receiver, EmailContent email
    ) {
        return full("key", "ORGANIZER_APPROVED", personKey, origin, originKey, receiver, email);
    }

    private EventData full(
            String key, String type, String personKey, String origin, String originKey,
            ReceiverContact receiver, EmailContent email
    ) {
        return new EventData(key, type, personKey, origin, originKey, receiver, email);
    }

    @Test
    void validate_completeData_hasNoViolations() {
        assertThat(validator.validate(dataOf("person", "ORGANIZATION", "origin", receiver, email))).isEmpty();
    }

    @Test
    void validate_withoutPersonKey_hasNoViolations() {
        assertThat(validator.validate(dataOf(null, "ORGANIZATION", "origin", receiver, email))).isEmpty();
    }

    @Test
    void validate_blankOrigin_hasViolation() {
        assertThat(validator.validate(dataOf(null, "", "origin", receiver, email))).hasSize(1);
    }

    @Test
    void validate_blankOriginKey_hasViolation() {
        assertThat(validator.validate(dataOf(null, "ORGANIZATION", " ", receiver, email))).hasSize(1);
    }

    @Test
    void validate_missingReceiverContact_hasViolation() {
        assertThat(validator.validate(dataOf(null, "ORGANIZATION", "origin", null, email))).hasSize(1);
    }

    @Test
    void validate_missingEmailBlock_hasViolation() {
        assertThat(validator.validate(dataOf(null, "ORGANIZATION", "origin", receiver, null))).hasSize(1);
    }

    @Test
    void validate_personKeyLongerThanColumn_hasViolation() {
        var longKey = "x".repeat(37);

        assertThat(validator.validate(dataOf(longKey, "ORGANIZATION", "origin", receiver, email))).hasSize(1);
    }

    @Test
    void validate_blankNotificationEventKey_hasViolation() {
        assertThat(validator.validate(full(" ", "ORGANIZER_APPROVED", null, "ORGANIZATION", "origin", receiver, email)))
                .hasSize(1);
    }

    @Test
    void validate_missingNotificationEventType_hasViolation() {
        assertThat(validator.validate(full("key", null, null, "ORGANIZATION", "origin", receiver, email)))
                .hasSize(1);
    }

    @Test
    void toDTO_completeData_mapsFieldsAndKeepsWholeEventDataAsJson() {
        var dto = dataOf("person", "ORGANIZATION", "origin", receiver, email).toDTO();

        assertThat(dto.notificationEventKey()).isEqualTo("key");
        assertThat(dto.notificationEventType()).isEqualTo("ORGANIZER_APPROVED");
        assertThat(dto.personKey()).isEqualTo("person");
        assertThat(dto.origin()).isEqualTo("ORGANIZATION");
        assertThat(dto.originKey()).isEqualTo("origin");
        assertThat(dto.receiverName()).isEqualTo("Maria");
        assertThat(dto.receiverEmail()).isEqualTo("maria@empresa.com");
        assertThat(dto.emailVariables()).containsEntry("legal_name", "x");
        assertThat(dto.data()).isEqualTo(
                "{\"notification_event_key\":\"key\",\"notification_event_type\":\"ORGANIZER_APPROVED\","
                        + "\"person_key\":\"person\",\"origin\":\"ORGANIZATION\",\"origin_key\":\"origin\","
                        + "\"receiver_contact\":{\"name\":\"Maria\",\"email\":\"maria@empresa.com\"},"
                        + "\"email\":{\"variables\":{\"legal_name\":\"x\"}}}");
    }

    @Test
    void toDTO_withoutPersonKey_keepsNull() {
        var dto = dataOf(null, "ORGANIZATION", "origin", receiver, email).toDTO();

        assertThat(dto.personKey()).isNull();
    }
}
