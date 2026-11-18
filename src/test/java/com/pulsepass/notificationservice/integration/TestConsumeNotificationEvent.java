package com.pulsepass.notificationservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.pulsepass.notificationservice.common.KafkaTools;
import com.pulsepass.notificationservice.common.MockTools;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestConsumeNotificationEvent extends BaseIntegrationTest {

    @Test
    void consumeNotificationEvent_organizerApproved_sendsApprovalEmail() {
        String eventKey = UUID.randomUUID().toString();
        String email = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String name = "Maria " + UUID.randomUUID().toString().substring(0, 8);
        String legalName = "Eventos " + UUID.randomUUID().toString().substring(0, 8) + " LTDA";
        String payload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", eventKey,
                "event_data", Map.of(
                        "notification_event_key", eventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", name, "email", email),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", name, "legal_name", legalName)))));

        MockTools.clearMocker();
        MockTools.addExpectation("mailtrap/post-send-email", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(eventKey, payload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 1);
        var requests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID);
        var sent = jsonMapper.valueToTree(requests.get(0)).get("body").get("json");

        assertThat(requests).hasSize(1);
        assertThat(sent.get("to").get(0).get("email").asString()).isEqualTo(email);
        assertThat(sent.get("to").get(0).get("name").asString()).isEqualTo(name);
        assertThat(sent.get("subject").asString()).isEqualTo("Seu cadastro no PulsePass foi aprovado");
        assertThat(sent.get("category").asString()).isEqualTo("organizer-approved");
        assertThat(sent.get("text").asString())
                .contains(name)
                .contains(legalName)
                .contains("http://localhost:3000");
        assertThat(sent.get("html").asString())
                .contains(name)
                .contains(legalName)
                .contains("http://localhost:3000");
    }

    @Test
    void consumeNotificationEvent_organizerRejected_sendsRejectionEmailWithReason() {
        String eventKey = UUID.randomUUID().toString();
        String email = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String name = "Joao " + UUID.randomUUID().toString().substring(0, 8);
        String legalName = "Producoes " + UUID.randomUUID().toString().substring(0, 8) + " LTDA";
        String reason = "Documento ilegivel " + UUID.randomUUID().toString().substring(0, 8);
        String payload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", eventKey,
                "event_data", Map.of(
                        "notification_event_key", eventKey,
                        "notification_event_type", "ORGANIZER_REJECTED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", name, "email", email),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", name,
                                "legal_name", legalName,
                                "rejection_reason_description", reason)))));

        MockTools.clearMocker();
        MockTools.addExpectation("mailtrap/post-send-email", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(eventKey, payload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 1);
        var requests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID);
        var sent = jsonMapper.valueToTree(requests.get(0)).get("body").get("json");

        assertThat(requests).hasSize(1);
        assertThat(sent.get("to").get(0).get("email").asString()).isEqualTo(email);
        assertThat(sent.get("subject").asString()).isEqualTo("Seu cadastro no PulsePass foi reprovado");
        assertThat(sent.get("category").asString()).isEqualTo("organizer-rejected");
        assertThat(sent.get("text").asString())
                .contains(name)
                .contains(legalName)
                .contains(reason);
        assertThat(sent.get("html").asString())
                .contains(name)
                .contains(legalName)
                .contains(reason);
    }

    @Test
    void consumeNotificationEvent_repeatedEventKey_sendsOnlyOneEmail() {
        String eventKey = UUID.randomUUID().toString();
        String email = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String name = "Ana " + UUID.randomUUID().toString().substring(0, 8);
        String legalName = "Shows " + UUID.randomUUID().toString().substring(0, 8) + " LTDA";
        String payload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", eventKey,
                "event_data", Map.of(
                        "notification_event_key", eventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", name, "email", email),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", name, "legal_name", legalName)))));

        MockTools.clearMocker();
        MockTools.addExpectation("mailtrap/post-send-email", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(eventKey, payload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 1);
        KafkaTools.publishNotificationEvent(eventKey, payload);
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 1);
        var requests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID);

        assertThat(requests).hasSize(1);
    }

    @Test
    void consumeNotificationEvent_providerRefusal_isHandledAndConsumptionContinues() {
        String failedEventKey = UUID.randomUUID().toString();
        String failedEmail = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String failedPayload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", failedEventKey,
                "event_data", Map.of(
                        "notification_event_key", failedEventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", "Rita", "email", failedEmail),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", "Contato", "legal_name", "Eventos Falha LTDA")))));
        MockTools.clearMocker();
        MockTools.addExpectation("mailtrap/post-send-email_bad_request", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(failedEventKey, failedPayload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 1);
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 1);
        final var refusedRequests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID);
        String nextEventKey = UUID.randomUUID().toString();
        String nextEmail = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String nextPayload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", nextEventKey,
                "event_data", Map.of(
                        "notification_event_key", nextEventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", "Paulo", "email", nextEmail),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", "Contato", "legal_name", "Eventos Seguinte LTDA")))));

        MockTools.addExpectation("mailtrap/post-send-email", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(nextEventKey, nextPayload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 2);
        var nextRequests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).stream()
                .filter(request -> nextEmail.equals(jsonMapper.valueToTree(request).get("body").get("json")
                        .get("to").get(0).get("email").asString()))
                .toList();
        var nextSent = jsonMapper.valueToTree(nextRequests.get(0)).get("body").get("json");

        assertThat(refusedRequests).hasSize(1);
        assertThat(nextRequests).hasSize(1);
        assertThat(nextSent.get("to").get(0).get("email").asString()).isEqualTo(nextEmail);
    }

    @Test
    void consumeNotificationEvent_providerDown_isRetriedAndDoesNotBlockOtherMessages() {
        String firstEventKey = UUID.randomUUID().toString();
        String firstEmail = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        final String firstPayload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", firstEventKey,
                "event_data", Map.of(
                        "notification_event_key", firstEventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", "Carla", "email", firstEmail),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", "Contato", "legal_name", "Eventos Primeiro LTDA")))));
        String secondEventKey = UUID.randomUUID().toString();
        String secondEmail = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        final String secondPayload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", secondEventKey,
                "event_data", Map.of(
                        "notification_event_key", secondEventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", "Davi", "email", secondEmail),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", "Contato", "legal_name", "Eventos Segundo LTDA")))));

        MockTools.clearMocker();
        MockTools.addExpectation("mailtrap/post-send-email_server_error", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        MockTools.addExpectation("mailtrap/post-send-email", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(firstEventKey, firstPayload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() >= 1);
        KafkaTools.publishNotificationEvent(secondEventKey, secondPayload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 4);
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 4);
        var requests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID);
        var recipients = requests.stream()
                .map(request -> jsonMapper.valueToTree(request).get("body").get("json").get("to").get(0)
                        .get("email").asString())
                .toList();

        assertThat(requests).hasSize(4);
        assertThat(recipients).containsOnly(firstEmail, secondEmail).contains(firstEmail, secondEmail);
    }

    @Test
    void consumeNotificationEvent_invalidJson_doesNotBlockNextMessage() {
        String invalidKey = UUID.randomUUID().toString();
        String eventKey = UUID.randomUUID().toString();
        String email = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        final String payload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", eventKey,
                "event_data", Map.of(
                        "notification_event_key", eventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", "Elisa", "email", email),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", "Contato", "legal_name", "Eventos Valido LTDA")))));

        MockTools.clearMocker();
        MockTools.addExpectation("mailtrap/post-send-email", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(invalidKey, "{\"event_type\": broken");
        KafkaTools.publishNotificationEvent(eventKey, payload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 1);
        var requests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID);
        var sent = jsonMapper.valueToTree(requests.get(0)).get("body").get("json");

        assertThat(requests).hasSize(1);
        assertThat(sent.get("to").get(0).get("email").asString()).isEqualTo(email);
    }

    @Test
    void consumeNotificationEvent_providerAlwaysDown_stopsAfterFiveAttemptsAndConsumptionContinues() {
        String downEventKey = UUID.randomUUID().toString();
        String downEmail = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String downPayload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", downEventKey,
                "event_data", Map.of(
                        "notification_event_key", downEventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", "Bruna", "email", downEmail),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", "Contato", "legal_name", "Eventos Fora LTDA")))));
        MockTools.clearMocker();
        MockTools.addExpectation(
                "mailtrap/post-send-email_service_unavailable", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(downEventKey, downPayload);
        await().atMost(Duration.ofSeconds(60)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 5);
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 5);
        final var downRequests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID);
        final var downRecipients = downRequests.stream()
                .map(request -> jsonMapper.valueToTree(request).get("body").get("json").get("to").get(0)
                        .get("email").asString())
                .toList();
        String nextEventKey = UUID.randomUUID().toString();
        String nextEmail = UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String nextPayload = jsonMapper.writeValueAsString(Map.of(
                "event_type", "NOTIFICATION_EVENT_CREATION",
                "event_key", nextEventKey,
                "event_data", Map.of(
                        "notification_event_key", nextEventKey,
                        "notification_event_type", "ORGANIZER_APPROVED",
                        "person_key", UUID.randomUUID().toString(),
                        "origin", "ORGANIZATION",
                        "origin_key", UUID.randomUUID().toString(),
                        "receiver_contact", Map.of("name", "Caio", "email", nextEmail),
                        "email", Map.of("variables", Map.of(
                                "recipient_name", "Contato", "legal_name", "Eventos Depois LTDA")))));

        MockTools.addExpectation("mailtrap/post-send-email", Map.of("sandbox_id", MAILTRAP_SANDBOX_ID));
        KafkaTools.publishNotificationEvent(nextEventKey, nextPayload);
        await().atMost(Duration.ofSeconds(30)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 6);
        await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10)).until(
                () -> MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).size() == 6);
        var nextRequests = MockTools.retrieveRequests("POST", "/api/send/" + MAILTRAP_SANDBOX_ID).stream()
                .filter(request -> nextEmail.equals(jsonMapper.valueToTree(request).get("body").get("json")
                        .get("to").get(0).get("email").asString()))
                .toList();
        var nextSent = jsonMapper.valueToTree(nextRequests.get(0)).get("body").get("json");

        assertThat(downRequests).hasSize(5);
        assertThat(downRecipients).containsOnly(downEmail);
        assertThat(nextRequests).hasSize(1);
        assertThat(nextSent.get("to").get(0).get("email").asString()).isEqualTo(nextEmail);
    }
}
