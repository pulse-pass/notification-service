package com.pulsepass.notificationservice.common;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public final class KafkaTools {

    private static final String NOTIFICATION_EVENT_TOPIC = "notification_event";

    private static String bootstrapServers = "localhost:9095";
    private static KafkaProducer<String, String> producer;

    private KafkaTools() {
    }

    public static void configure(String newBootstrapServers) {
        bootstrapServers = newBootstrapServers;
    }

    public static void publishNotificationEvent(String key, String value) {
        if (producer == null) {
            var properties = new Properties();
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producer = new KafkaProducer<>(properties);
        }
        try {
            producer.send(new ProducerRecord<>(NOTIFICATION_EVENT_TOPIC, key, value)).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
