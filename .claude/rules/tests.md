---
description: Unit and integration testing strategy and conventions
paths:
  - "src/test/**"
---

# Tests

## Unit tests

- **NTF-156** Unit tests (JUnit 5 + Mockito + AssertJ) in `src/test/java/.../unit/`, mirroring the main packages: one test class per production class, never grouping several classes in one test.
- **NTF-157** Test names `method_scenario_expectedResult`.
- **NTF-158** External gateways (`infrastructure/gateways`) are unit-tested against a local stub server in the test package that records what it receives (`StubHttpServer`, in `unit/infrastructure/gateways/`, for the e-mail gateway); `RedisCache` (`infrastructure/redis`) is tested with a mocked `StringRedisTemplate`.
- **NTF-159** The attempts and every status transition (`PENDING`, `SENT`, `FAILED`, the repeated key, the type not found, the invalid message, the unsupported envelope type) are covered by unit tests of the service, the consumer, the message and the Kafka configuration; the retry configuration (annotation of the consumer, topic `notification_event_retry`, no fatal exception) is covered by unit tests of the consumer, `NotificationEventKafkaConfig` and `NotificationEventRetryTopicConfig`, the latter proving with a `DefaultDestinationTopicResolver` that conversion, deserialization, validation, cast and domain failures all go to the retry topic.
- **NTF-160** Unit tests may keep constants, `@BeforeEach` setup and private helpers.

## Integration tests

- **NTF-161** Black-box integration tests against the consumer running in Docker Compose (no Testcontainers), in `src/test/java/.../integration/`: `BaseIntegrationTest` (Kafka bootstrap servers `localhost:9095`, MockServer `http://localhost` port `1083` and sandbox id `1234567` from environment variables with those defaults) and one class per topic.
- **NTF-162** Classes are `TestConsume<Topic>` (e.g. `TestConsumeNotificationEvent`).
- **NTF-163** They run with `./mvnw test -Pintegration`; `./mvnw test` runs only the unit tests.
- **NTF-164** REST Assured must be 6.x: Spring Boot 4 brings Groovy 5 and Jackson 3, which REST Assured 5.x cannot use; it drives the MockServer API.
- **NTF-165** Integration tests are always run against the real compose (`./infrastructure/service-start.sh test` + `./mvnw test -Pintegration`) before a task is closed; unit tests with mocks do not catch persistence problems (constraints, schema validation).
- **NTF-166** Integration tests publish to Kafka and check the result through the requests the MockServer received (`MockTools.retrieveRequests`, NTF-190 onwards); they never open a database connection (JDBC, `DriverManager`, SQL strings) or touch the schema.
- **NTF-167** Waiting for an asynchronous result uses Awaitility with a timeout, never `Thread.sleep`; the absence of a new request is checked after a wait long enough for the consumer to have processed the message (`during`, NTF-198).
- **NTF-168** A refusal of the provider is provoked with the `post-send-email_bad_request` expectation (status 400, `times` 1, NTF-201); the test waits for that single request, checks that no other one arrives, registers the success expectation after it (without clearing the mocker) and publishes another message with another key, and checks, counting only the requests of its recipient, that its request arrives, which proves the refusal was handled and the consumption went on.
- **NTF-200** A provider that is down is provoked with the `post-send-email_server_error` expectation (status 500, `times` 2) registered together with the success one: the test publishes a message, waits for its first request, publishes another, and checks that after the two failures both recipients got their e-mail through the retry topic (4 requests in all, no new one afterwards); a broken JSON published before a valid message proves the unmapped error does not block the partition, and the valid one is sent normally. A provider that does not recover within the send limit is provoked with `post-send-email_service_unavailable` (status 500, `times` 5, the 5 attempts of NTF-204): the test waits for exactly 5 requests for that recipient, checks with `during` that no other arrives (the message left the queue, NTF-204), then registers the success expectation after it (without clearing the mocker) and checks, counting only the requests of the next recipient, that another message is sent normally. The test of the retry that does not block other messages cannot make the provider fail for one recipient only (the templates do not match the body, NTF-196), so it proves the retry and the delivery of both recipients, not their order.
- **NTF-169** Integration test classes have no constants other than the topics the class uses directly.
- **NTF-170** Each test declares its own data (event key, e-mail address, names), builds its own message and writes its own assertions inline, even when that repeats code across tests.
- **NTF-171** Stateless tools (`JsonMapper`) are fine as instance fields, and `BaseIntegrationTest` keeps its shared constants and fields (bootstrap servers, MockServer URL, sandbox id).
- **NTF-172** Messages in integration tests are declared in a single expression with `Map.of` and `List.of`, nested objects included, never with `new HashMap<>()` plus `put` or JSON concatenated in a string.
- **NTF-173** Above 10 entries use `Map.ofEntries(Map.entry(...), ...)`. `HashMap` is only for a test that needs an explicit `null` value or a mutable map.
- **NTF-174** Each test generates its own data (UUIDs, a unique recipient e-mail address); no teardown, and every test starts with `MockTools.clearMocker()` (NTF-190 onwards).
- **NTF-175** Every behavior of the topic has a success test and an error test (approval, rejection, repeated key, provider refusal handled with the consumption continuing, provider down retried without blocking, broken JSON that does not block the next message); since an unmapped error goes to the retry topic (NTF-186) it no longer poisons the queue, but a test must never leave a message that fails forever calling the provider, because its retries would reach the next tests.
- **NTF-176** No comments in test code (`//`, `/* */`, Javadoc).

## Shared helpers

- **NTF-177** Shared test helpers live in `src/test/java/.../common/` and are the only reusable test code.
- **NTF-178** The no-constants rule applies to them too, except for URLs and topic names; stateless tools (`JsonMapper`) are fine as fields.
- **NTF-179** Test helpers are not named `<Subject>Handlers`, which belongs to the production `common/` and would clash with it (same package).
