---
description: Local infrastructure, Docker and scripts conventions
paths:
  - "infrastructure/**"
  - "Dockerfile"
  - "README.md"
  - "src/main/resources/application*.properties"
---

# Infrastructure

- **NTF-012** `infrastructure/` holds `docker-compose.yml`, `.env.example`, `database.sql`, `service-start.sh` and `service-stop.sh`, plus the external `pulse-pass` network.
- **NTF-013** The service has no HTTP API: its only container is `notification_event-consumer`, the same application started with the `notification_event-consumer` profile.
- **NTF-014** The compose declares `name: <repo>-test` and gives every variable the scripts export (`SERVICE_PREFIX`, `MYSQL_HOST`, each `<X>_URL`) a `test` mode default, e.g. `${SERVICE_PREFIX:-<repo>-test}`.
- **NTF-015** Because of those defaults, `docker compose` without the scripts resolves to test mode (the CI log step relies on it); the scripts still pass `-p` and export those variables for the chosen mode.
- **NTF-016** The service keys of the compose are generic (`mysql`, `redis`, `kafka`, `mockserver`) except the consumer, whose key is its name (`notification_event-consumer`); the real name comes from `container_name` with `SERVICE_PREFIX`, because the key becomes a DNS alias on the `pulse-pass` network and both modes run together.
- **NTF-017** Always address another container by its `container_name`, never by the compose key.
- **NTF-018** `test` mode (`./infrastructure/service-start.sh test`, default, used by the CI) is self-sufficient: project `<repo>-test`, containers `<repo>-test-mysql`, `<repo>-test-redis`, `<repo>-test-kafka`, `<repo>-test-mockserver` and `<repo>-test-notification_event-consumer`; it never depends on `pulse-pass-infrastructure`.
- **NTF-019** `test` mode has its own MySQL (profile `test`, `database.sql` mounted in `initdb`), Redis, Kafka (KRaft, advertised as `<prefix>-kafka:9092` inside the network and `localhost:${KAFKA_PORT}` outside) and a MockServer (`mockserver/mockserver`) that plays the Mailtrap API.
- **NTF-020** Ports of `test` mode: MySQL 3309, Redis 6381, Kafka 9095 and MockServer 1083; the consumer publishes none.
- **NTF-021** The MySQL healthcheck pings over TCP (`mysqladmin ping -h 127.0.0.1`) so it only passes after `database.sql` has run.
- **NTF-022** The consumer healthcheck is `test -f /tmp/notification_event-consumer.ready`, a file the application writes when the partitions are assigned.
- **NTF-023** `development` mode starts only the consumer: project `<repo>`, container `<repo>-notification_event-consumer`, using the shared `pulse-pass-mysql`, `pulse-pass-redis`, and `pulse-pass-kafka` from `pulse-pass-infrastructure`, and the real Mailtrap Email Sandbox API.
- **NTF-024** Before anything else (before calling `service-stop.sh`, so a missing infrastructure never takes down a running consumer), the script requires those three containers to be `healthy`, otherwise it exits asking to start the shared infrastructure.
- **NTF-025** As root (`MYSQL_ROOT_PASSWORD`, `root` like the infrastructure), the script creates the `.env` user with `CREATE USER IF NOT EXISTS` and a `GRANT ALL PRIVILEGES` only on the service schema, then applies `database.sql`.
- **NTF-026** The `.env` user, password and schema must not contain quotes or backslashes, because the script writes them into SQL.
- **NTF-027** Table changes only reach the shared MySQL when the script runs again; it is idempotent and never recreates what exists.
- **NTF-028** Both modes can run at the same time (distinct projects, container names and ports).
- **NTF-029** `service-stop.sh [test|development]` (default `test`) tears down only that mode's project (`down -v --remove-orphans --rmi local`) and never touches the shared containers or the schema; `service-start.sh <mode>` calls it for the same mode first.
- **NTF-030** The ports 3306, 6379, 9094 and 8180 belong to the shared infrastructure and the service must not publish them.
- **NTF-031** The mode comes from the argument of the scripts, never from editing `.env`.
- **NTF-032** Each external dependency has a `<X>_TEST_URL` / `<X>_DEVELOPMENT_URL` pair in `.env` (`KAFKA`, `REDIS`, `MAILTRAP`), and the script exports `<X>_URL`, which feeds the property of the dependency (`spring.kafka.bootstrap-servers`, `spring.data.redis.url`, `app.mailtrap.url`); the test URLs point to the `<repo>-test-*` containers (`MAILTRAP_TEST_URL` to the MockServer) and the development URL to `https://sandbox.api.mailtrap.io`.
- **NTF-033** The start script creates the `pulse-pass` network if missing and copies `.env.example` on first run.
- **NTF-034** Keycloak, Kafka UI, MinIO and the shared MySQL, Redis and Kafka live in the `pulse-pass-infrastructure` repository.
- **NTF-035** README: running the service only needs Docker (the image builds with its own JDK); Java 21 is only needed to run `./mvnw` (lint and tests) outside the container.
- **NTF-036** No comments in `infrastructure/`, `Dockerfile` or `application.properties`: names and structure must explain them, and decisions go to the rules or the README.
- **NTF-037** Generated or third-party files are kept as they come, comments included (`mvnw`, `checkstyle.xml`).
- **NTF-038** Secrets in `.env.example` are placeholders or fixed test values; the real value goes only to the git-ignored `.env`.
- **NTF-039** A value in `.env` that has spaces, `<`, `>` or `$`, such as `MAILTRAP_API_TOKEN='your-mailtrap-api-token'`, goes in single quotes.
- **NTF-040** Quotes are needed because `service-start.sh` sources the file and Docker Compose interpolates it, and without quotes the shell fails or the value silently becomes empty.
- **NTF-210** `NOTIFICATION_EVENT_RETRY_DELAY` is `60000` in `.env.example` (the `development` value); `service-start.sh` exports `500` in the `test` branch, after sourcing `.env`, so the integration tests wait milliseconds for a retry, and the compose passes it to the consumer in `environment` with the `test` default `500` (NTF-014), which wins over `env_file`; the mode never comes from editing `.env` (NTF-031).
