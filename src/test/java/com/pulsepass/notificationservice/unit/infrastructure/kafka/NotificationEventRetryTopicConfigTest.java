package com.pulsepass.notificationservice.unit.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.api.consumers.NotificationEventConsumer;
import com.pulsepass.notificationservice.api.messages.NotificationEventMessage;
import com.pulsepass.notificationservice.domain.exceptions.InvalidNotificationEventException;
import com.pulsepass.notificationservice.infrastructure.kafka.NotificationEventRetryTopicConfig;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.retrytopic.DefaultDestinationTopicResolver;
import org.springframework.kafka.retrytopic.DestinationTopic;
import org.springframework.kafka.retrytopic.DestinationTopicPropertiesFactory;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicComponentFactory;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.ExceptionMatcher;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.validation.BindingResult;

class NotificationEventRetryTopicConfigTest {

    private static final String LISTENER_ID = "notification-service-notification_event-consumer";

    private DefaultDestinationTopicResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = resolverOf(new NotificationEventRetryTopicConfig());
    }

    @SuppressWarnings("unchecked")
    private DefaultDestinationTopicResolver resolverOf(RetryTopicConfigurationSupport support) {
        ObjectProvider<RetryTopicComponentFactory> provider = mock(ObjectProvider.class);
        when(provider.getIfUnique(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<RetryTopicComponentFactory>) invocation.getArgument(0)).get());
        var created = (DefaultDestinationTopicResolver) support.destinationTopicResolver(provider);
        var properties = new DestinationTopicPropertiesFactory(
                "_retry", "_dlt", List.of(500L, 500L, 500L, 500L),
                ExceptionMatcher.forDenyList().build(), 3, mock(KafkaOperations.class), DltStrategy.NO_DLT,
                TopicSuffixingStrategy.SUFFIX_WITH_DELAY_VALUE, SameIntervalTopicReuseStrategy.SINGLE_TOPIC, -1L)
                .createProperties();
        created.addDestinationTopics(LISTENER_ID, properties.stream()
                .map(property -> new DestinationTopic("notification_event" + property.suffix(), property))
                .toList());
        return created;
    }

    private String nextTopicOf(Exception exception) {
        var failure = new ListenerExecutionFailedException("failed", exception);
        return resolver.resolveDestinationTopic(LISTENER_ID, "notification_event", 1, failure, 0L).getDestinationName();
    }

    @Test
    void destinationTopicResolver_springDefaultSupport_discardsConversionFailure() {
        var defaultResolver = resolverOf(new RetryTopicConfigurationSupport());
        var failure = new ListenerExecutionFailedException("failed", new MessageConversionException("broken json"));

        var next = defaultResolver.resolveDestinationTopic(LISTENER_ID, "notification_event", 1, failure, 0L);

        assertThat(next.isNoOpsTopic()).isTrue();
    }

    @Test
    void destinationTopicResolver_conversionFailure_goesToRetryTopic() {
        assertThat(nextTopicOf(new MessageConversionException("broken json"))).isEqualTo("notification_event_retry");
    }

    @Test
    void destinationTopicResolver_deserializationFailure_goesToRetryTopic() {
        var failure = new DeserializationException("broken", new byte[0], false, new IllegalStateException());

        assertThat(nextTopicOf(failure)).isEqualTo("notification_event_retry");
    }

    @Test
    void destinationTopicResolver_validationFailure_goesToRetryTopic() throws Exception {
        var parameter = new MethodParameter(
                NotificationEventConsumer.class.getMethod("consume", NotificationEventMessage.class), 0);
        var failure = new MethodArgumentNotValidException(mock(Message.class), parameter, mock(BindingResult.class));

        assertThat(nextTopicOf(failure)).isEqualTo("notification_event_retry");
    }

    @Test
    void destinationTopicResolver_classCastFailure_goesToRetryTopic() {
        assertThat(nextTopicOf(new ClassCastException())).isEqualTo("notification_event_retry");
    }

    @Test
    void destinationTopicResolver_domainFailure_goesToRetryTopic() {
        assertThat(nextTopicOf(new InvalidNotificationEventException())).isEqualTo("notification_event_retry");
    }

    @Test
    void destinationTopicResolver_anyFailureFromRetryTopic_staysOnRetryTopic() {
        var failure = new ListenerExecutionFailedException("failed", new IllegalStateException("unmapped"));

        var next = resolver.resolveDestinationTopic(LISTENER_ID, "notification_event_retry", 2, failure, 0L);

        assertThat(next.getDestinationName()).isEqualTo("notification_event_retry");
    }

    @Test
    void notificationEventRetryTaskScheduler_always_createsThreadPoolScheduler() {
        var scheduler = new NotificationEventRetryTopicConfig().notificationEventRetryTaskScheduler();

        assertThat(scheduler).isInstanceOf(ThreadPoolTaskScheduler.class);
        assertThat(((ThreadPoolTaskScheduler) scheduler).getThreadNamePrefix()).isEqualTo("notification_event-retry-");
    }
}
