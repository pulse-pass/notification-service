package com.pulsepass.notificationservice.unit.api.messages;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.api.messages.NotificationEventMessageTypeEnum;
import org.junit.jupiter.api.Test;

class NotificationEventMessageTypeEnumTest {

    @Test
    void values_enumDeclaration_listsOnlyNotificationEventCreation() {
        assertThat(NotificationEventMessageTypeEnum.values())
                .containsExactly(NotificationEventMessageTypeEnum.NOTIFICATION_EVENT_CREATION);
    }

    @Test
    void valueOf_actionName_returnsMatchingConstant() {
        assertThat(NotificationEventMessageTypeEnum.valueOf("NOTIFICATION_EVENT_CREATION"))
                .isEqualTo(NotificationEventMessageTypeEnum.NOTIFICATION_EVENT_CREATION);
        assertThat(NotificationEventMessageTypeEnum.NOTIFICATION_EVENT_CREATION.name())
                .isEqualTo("NOTIFICATION_EVENT_CREATION");
    }
}
