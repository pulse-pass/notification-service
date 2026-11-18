package com.pulsepass.notificationservice.infrastructure.kafka;

import java.time.Duration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class NotificationEventKafkaConfig implements KafkaListenerConfigurer {

    private static final String TOPIC = "notification_event";
    private static final String RETRY_TOPIC = "notification_event_retry";
    private static final int PARTITIONS = 1;
    private static final int CONCURRENCY = 1;

    private final LocalValidatorFactoryBean validator;

    public NotificationEventKafkaConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    @Bean
    public NewTopic notificationEventTopic() {
        return TopicBuilder.name(TOPIC)
                .partitions(PARTITIONS)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
                .build();
    }

    @Bean
    public NewTopic notificationEventRetryTopic() {
        return TopicBuilder.name(RETRY_TOPIC)
                .partitions(PARTITIONS)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
                .build();
    }

    @Bean
    public RetryTopicConfiguration notificationEventRetryTopicConfiguration(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.notification-event.retry.max-attempts}") int maxAttempts,
            @Value("${app.notification-event.retry.delay}") long delay
    ) {
        return RetryTopicConfigurationBuilder.newInstance()
                .includeTopic(TOPIC)
                .maxAttempts(maxAttempts)
                .fixedBackOff(delay)
                .retryTopicSuffix("_retry")
                .sameIntervalTopicReuseStrategy(SameIntervalTopicReuseStrategy.SINGLE_TOPIC)
                .doNotConfigureDlt()
                .doNotAutoCreateRetryTopics()
                .listenerFactory("kafkaListenerContainerFactory")
                .create(kafkaTemplate);
    }

    @Bean
    public StringJacksonJsonMessageConverter notificationEventMessageConverter(JsonMapper jsonMapper) {
        return new StringJacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            StringJacksonJsonMessageConverter notificationEventMessageConverter,
            OpenEntityManagerInRecordInterceptor openEntityManagerInRecordInterceptor,
            ConsumerReadinessListener consumerReadinessListener,
            @Value("${app.consumers.notification-event}") boolean autoStartup
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(CONCURRENCY);
        factory.setAutoStartup(autoStartup);
        factory.setRecordMessageConverter(notificationEventMessageConverter);
        factory.setRecordInterceptor(openEntityManagerInRecordInterceptor);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.getContainerProperties().setConsumerRebalanceListener(consumerReadinessListener);
        return factory;
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }
}
