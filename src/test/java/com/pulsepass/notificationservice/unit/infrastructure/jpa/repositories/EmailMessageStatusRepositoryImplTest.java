package com.pulsepass.notificationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.infrastructure.jpa.entities.EmailMessageStatusEntity;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.EmailMessageStatusRepositoryImpl;
import com.pulsepass.notificationservice.infrastructure.jpa.repositories.JpaEmailMessageStatusRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EmailMessageStatusRepositoryImplTest {

    private final JpaEmailMessageStatusRepository jpaRepository = mock(JpaEmailMessageStatusRepository.class);
    private final EmailMessageStatusRepositoryImpl repository = new EmailMessageStatusRepositoryImpl(jpaRepository);

    @Test
    void findByEnumerator_existingEnumerator_returnsDomain() {
        when(jpaRepository.findByEnumerator("PENDING"))
                .thenReturn(Optional.of(new EmailMessageStatusEntity(1L, "PENDING", null)));

        var result = repository.findByEnumerator("PENDING");

        assertThat(result).isPresent();
        assertThat(result.get().getEnumerator()).isEqualTo("PENDING");
    }

    @Test
    void findByEnumerator_unknownEnumerator_returnsEmpty() {
        when(jpaRepository.findByEnumerator("X")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("X")).isEmpty();
    }
}
