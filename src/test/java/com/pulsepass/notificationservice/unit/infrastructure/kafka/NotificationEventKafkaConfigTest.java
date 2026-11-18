package com.pulsepass.notificationservice.unit.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.pulsepass.notificationservice.infrastructure.kafka.ConsumerReadinessListener;
import com.pulsepass.notificationservice.infrastructure.kafka.NotificationEventKafkaConfig;
import com.pulsepass.notificationservice.infrastructure.kafka.OpenEntityManagerInRecordInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.json.JsonMapper;

class NotificationEventKafkaConfigTest {

    private LocalValidatorFactoryBean validator;
    private NotificationEventKafkaConfig config;

    @BeforeEach
    void setUp() {
        validator = mock(LocalValidatorFactoryBean.class);
        config = new NotificationEventKafkaConfig(validator);
    }

    @Test
    void notificationEventTopic_always_hasOnePartitionAndSevenDaysRetention() {
        var topic = config.notificationEventTopic();

        assertThat(topic.name()).isEqualTo("notification_event");
        assertThat(topic.numPartitions()).isEqualTo(1);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
        assertThat(topic.configs()).containsEntry("retention.ms", "604800000");
    }

    @Test
    void notificationEventRetryTopic_always_hasOnePartitionAndSevenDaysRetention() {
        var topic = config.notificationEventRetryTopic();

        assertThat(topic.name()).isEqualTo("notification_event_retry");
        assertThat(topic.numPartitions()).isEqualTo(1);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
        assertThat(topic.configs()).containsEntry("retention.ms", "604800000");
    }

    @Test
    void notificationEventRetryTopicConfiguration_properties_declaresSingleRetryTopicWithoutDlt() {
        var configuration = config.notificationEventRetryTopicConfiguration(mock(KafkaTemplate.class), 10081, 60000L);

        var destinations = configuration.getDestinationTopicProperties();

        assertThat(configuration.hasConfigurationForTopics(new String[] {"notification_event"})).isTrue();
        assertThat(destinations).hasSize(2);
        assertThat(destinations.get(0).suffix()).isEmpty();
        assertThat(destinations.get(1).suffix()).isEqualTo("_retry");
        assertThat(destinations.get(1).delay()).isEqualTo(60000L);
        assertThat(destinations).noneMatch(destination -> destination.isDltTopic());
    }

    @Test
    void notificationEventMessageConverter_jsonMapper_isCreated() {
        assertThat(config.notificationEventMessageConverter(JsonMapper.builder().build())).isNotNull();
    }

    @Test
    void kafkaListenerContainerFactory_dependencies_setsAckModeReadinessListenerAndConcurrency() {
        var readiness = new ConsumerReadinessListener("/tmp/notification_event-consumer.ready");
        var interceptor = mock(OpenEntityManagerInRecordInterceptor.class);

        var factory = config.kafkaListenerContainerFactory(
                mock(ConsumerFactory.class),
                config.notificationEventMessageConverter(JsonMapper.builder().build()),
                interceptor,
                readiness,
                false);

        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.RECORD);
        assertThat(factory.getContainerProperties().getConsumerRebalanceListener()).isSameAs(readiness);
        assertThat(ReflectionTestUtils.getField(factory, "concurrency")).isEqualTo(1);
        assertThat(ReflectionTestUtils.getField(factory, "autoStartup")).isEqualTo(false);
    }

    @Test
    void configureKafkaListeners_registrar_usesBeanValidator() {
        var registrar = mock(KafkaListenerEndpointRegistrar.class);

        config.configureKafkaListeners(registrar);

        verify(registrar).setValidator(validator);
    }
}
