package com.pulsepass.notificationservice.unit.api.messages.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.api.messages.definitions.EmailContent;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmailContentTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_withVariables_hasNoViolations() {
        assertThat(validator.validate(new EmailContent(Map.of("legal_name", "x")))).isEmpty();
    }

    @Test
    void validate_missingVariables_hasViolation() {
        assertThat(validator.validate(new EmailContent(null))).hasSize(1);
    }
}
