package com.pulsepass.notificationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.domain.entities.EmailMessageStatusEnum;
import org.junit.jupiter.api.Test;

class EmailMessageStatusEnumTest {

    @Test
    void values_enumDeclaration_listsStatusesInLifecycleOrder() {
        assertThat(EmailMessageStatusEnum.values())
                .containsExactly(EmailMessageStatusEnum.PENDING, EmailMessageStatusEnum.SENT,
                        EmailMessageStatusEnum.FAILED);
    }

    @Test
    void valueOf_statusName_returnsMatchingConstant() {
        assertThat(EmailMessageStatusEnum.valueOf("SENT")).isEqualTo(EmailMessageStatusEnum.SENT);
        assertThat(EmailMessageStatusEnum.FAILED.name()).isEqualTo("FAILED");
    }
}
