package com.pulsepass.notificationservice.unit.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.pulsepass.notificationservice.infrastructure.kafka.ConsumerReadinessListener;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConsumerReadinessListenerTest {

    @TempDir
    Path tempDir;

    @Test
    void onPartitionsAssigned_partitions_writesReadyFile() {
        var readyFile = tempDir.resolve("notification_event-consumer.ready");
        var listener = new ConsumerReadinessListener(readyFile.toString());

        listener.onPartitionsAssigned(mock(Consumer.class), List.of(new TopicPartition("notification_event", 0)));

        assertThat(readyFile).exists();
    }

    @Test
    void onPartitionsAssigned_twice_keepsReadyFile() {
        var readyFile = tempDir.resolve("notification_event-consumer.ready");
        var listener = new ConsumerReadinessListener(readyFile.toString());

        listener.onPartitionsAssigned(mock(Consumer.class), List.of());
        listener.onPartitionsAssigned(mock(Consumer.class), List.of());

        assertThat(Files.exists(readyFile)).isTrue();
    }

    @Test
    void onPartitionsAssigned_unwritablePath_throwsUnchecked() {
        var listener = new ConsumerReadinessListener(tempDir.resolve("missing/dir/ready").toString());

        assertThatThrownBy(() -> listener.onPartitionsAssigned(mock(Consumer.class), List.of()))
                .isInstanceOf(UncheckedIOException.class);
    }
}
