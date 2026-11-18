package com.pulsepass.notificationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationEventTypeEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.JpaNotificationEventTypeRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.NotificationEventTypeRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NotificationEventTypeRepositoryImplTest {

    private final JpaNotificationEventTypeRepository jpaRepository = mock(JpaNotificationEventTypeRepository.class);
    private final NotificationEventTypeRepositoryImpl repository =
            new NotificationEventTypeRepositoryImpl(jpaRepository);

    @Test
    void findByEnumerator_existingEnumerator_returnsDomain() {
        when(jpaRepository.findByEnumerator("ORGANIZER_APPROVED")).thenReturn(Optional.of(
                new NotificationEventTypeEntity(1L, "ORGANIZER_APPROVED", "subject", "template", "legal_name", null)));

        var result = repository.findByEnumerator("ORGANIZER_APPROVED");

        assertThat(result).isPresent();
        assertThat(result.get().getEmailTemplate()).isEqualTo("template");
    }

    @Test
    void findByEnumerator_unknownEnumerator_returnsEmpty() {
        when(jpaRepository.findByEnumerator("X")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("X")).isEmpty();
    }
}
