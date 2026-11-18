package com.pulsepass.notificationservice.infrastructure.kafka;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerReadinessListener implements ConsumerAwareRebalanceListener {

    private final Path readyFile;

    public ConsumerReadinessListener(@Value("${app.consumers.ready-file}") String readyFile) {
        this.readyFile = Path.of(readyFile);
    }

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        try {
            Files.writeString(readyFile, "ready");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
