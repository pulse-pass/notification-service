package com.pulsepass.notificationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.NotificationOriginEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.JpaNotificationOriginRepository;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.NotificationOriginRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NotificationOriginRepositoryImplTest {

    private final JpaNotificationOriginRepository jpaRepository = mock(JpaNotificationOriginRepository.class);
    private final NotificationOriginRepositoryImpl repository = new NotificationOriginRepositoryImpl(jpaRepository);

    @Test
    void findByEnumerator_existingEnumerator_returnsDomain() {
        when(jpaRepository.findByEnumerator("ORGANIZATION"))
                .thenReturn(Optional.of(new NotificationOriginEntity(1L, "ORGANIZATION", null)));

        var result = repository.findByEnumerator("ORGANIZATION");

        assertThat(result).isPresent();
        assertThat(result.get().getEnumerator()).isEqualTo("ORGANIZATION");
    }

    @Test
    void findByEnumerator_unknownEnumerator_returnsEmpty() {
        when(jpaRepository.findByEnumerator("X")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("X")).isEmpty();
    }
}
