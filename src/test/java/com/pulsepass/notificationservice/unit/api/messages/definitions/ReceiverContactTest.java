package com.pulsepass.notificationservice.unit.api.messages.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.api.messages.definitions.ReceiverContact;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ReceiverContactTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_nameAndEmail_hasNoViolations() {
        assertThat(validator.validate(new ReceiverContact("Maria", "maria@empresa.com"))).isEmpty();
    }

    @Test
    void validate_blankName_hasViolation() {
        assertThat(validator.validate(new ReceiverContact(" ", "maria@empresa.com"))).hasSize(1);
    }

    @Test
    void validate_missingEmail_hasViolation() {
        assertThat(validator.validate(new ReceiverContact("Maria", null))).hasSize(1);
    }

    @Test
    void validate_malformedEmail_hasViolation() {
        assertThat(validator.validate(new ReceiverContact("Maria", "maria-at-empresa"))).hasSize(1);
    }
}
