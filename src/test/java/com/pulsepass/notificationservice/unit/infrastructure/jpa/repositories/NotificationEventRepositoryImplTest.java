package com.pulsepass.notificationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.JpaNotificationEventRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.NotificationEventRepositoryImpl;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

class NotificationEventRepositoryImplTest {

    private final JpaNotificationEventRepository jpaRepository = mock(JpaNotificationEventRepository.class);
    private final NotificationEventRepositoryImpl repository = new NotificationEventRepositoryImpl(jpaRepository);

    private NotificationEventEntity entity() {
        return new NotificationEventEntity(
                1L, "event-key", null, "origin-key",
                new NotificationEventTypeEntity(1L, "ORGANIZER_APPROVED", "s", "t", "legal_name", null),
                new NotificationOriginEntity(1L, "ORGANIZATION", null),
                "{}", null, null);
    }

    @Test
    void save_newEvent_returnsSavedDomain() {
        when(jpaRepository.save(any(NotificationEventEntity.class))).thenReturn(entity());
        var event = new NotificationEvent();
        event.setNotificationEventType(new NotificationEventType());
        event.setNotificationOrigin(new NotificationOrigin());

        var result = repository.save(event);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNotificationEventKey()).isEqualTo("event-key");
    }

    @Test
    void findForUpdateByNotificationEventKey_existingKey_returnsDomain() {
        when(jpaRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.of(entity()));

        assertThat(repository.findForUpdateByNotificationEventKey("event-key")).isPresent();
    }

    @Test
    void findForUpdateByNotificationEventKey_unknownKey_returnsEmpty() {
        when(jpaRepository.findForUpdateByNotificationEventKey("x")).thenReturn(Optional.empty());

        assertThat(repository.findForUpdateByNotificationEventKey("x")).isEmpty();
    }

    @Test
    void findForUpdateByNotificationEventKey_jpaMethod_usesPessimisticWriteLock() throws Exception {
        var method = JpaNotificationEventRepository.class
                .getMethod("findForUpdateByNotificationEventKey", String.class);

        assertThat(method.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
