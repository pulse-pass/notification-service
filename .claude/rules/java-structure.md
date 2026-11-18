---
description: Stack, packages, shared code, entities and common/
paths:
  - "src/main/java/**/*.java"
---

# Java structure

- **NTF-128** Java 21, Spring Boot 4.x, Maven (no Gradle, no Lombok).
- **NTF-129** Package `com.pulsepass.notificationservice`.
- **NTF-130** Packages: `api` (`consumers`, `messages`), `application` (services, dtos/input), `domain` (entities, repositories, gateways, exceptions, `UnitOfWork`, `Cache`), `infrastructure` (`jpa`, `gateways`, `kafka`, `redis`) and `common`.
- **NTF-131** Code shared by every service is identical in all of them except for the package, tests included.
- **NTF-132** The shared code in this repository is `DomainException`, `UnitOfWork` and `JsonHandlers`, with the test of `JsonHandlers`.
- **NTF-133** `JpaUnitOfWork` is a deviation from the shared code: without HTTP there is no `@RequestScope`, so it is a singleton `@Component` that keeps the `TransactionStatus` in a `ThreadLocal`, with the same methods and semantics as the request-scoped one and no `@PreDestroy`; the `OpenEntityManagerInRecordInterceptor` does the `cleanup()` by calling `rollback()` when `isActive()` (NTF-110).
- **NTF-134** A change to a shared class (a rename, a new case, a new method) is applied to every repository that has it in the same task; before closing a task, diff these files across the repositories.
- **NTF-135** Pure domain entity + JPA `*Entity` with `fromDomain`/`toDomain`; repository interface in the domain, implementation in `infrastructure/jpa`.
- **NTF-136** JPA `*Entity` classes have only fields, constructors and `fromDomain`/`toDomain` — no getters/setters (Hibernate uses field access); this also keeps them from being flagged as duplicates of the domain entities.
- **NTF-137** Inside entities (domain and JPA), always reference the object's own fields with `this.` (getters, `toDomain`, `toHistoryData`).
- **NTF-138** Never read another entity's fields directly (e.g. `history.emailMessage.status`); go through a method such as `toDomain()`.
- **NTF-139** Logic that builds data from the entity itself (e.g. the history snapshot, `toHistoryData()`) belongs to the domain, not to the service.
- **NTF-140** The Kafka message is a record in `api/messages` (with `definitions/` for the nested objects) that validates itself with Bean Validation and converts with `toDTO()`, which only delegates to `event_data` (`EventData.toDTO()`); the service receives the DTO from `application/dtos/input`.
- **NTF-141** `common/` holds only general-purpose language helpers (string, JSON), named `<Subject>Handlers` (`JsonHandlers`) and placed directly in `common/`, without a `utils` subfolder.
- **NTF-142** Framework concerns (Kafka configuration, Spring beans) never go to `common/`: they live in `infrastructure/kafka` or in the class that uses them.
- **NTF-183** Reading JSON into an object (`JsonParseHandlers.fromJson`) is a helper of this service, separate from the shared `JsonHandlers` so that one stays identical in every repository (NTF-131).
- **NTF-184** Redis is infrastructure of the service, like the database, not a gateway: it is accessed through the domain interface `Cache` (`acquireLock(key, ttl)` with `SET NX` and a TTL, `delete(key)`), implemented by `infrastructure/redis/RedisCache` on `StringRedisTemplate`, and every key is prefixed with the service name (`spring.application.name` + `:`, e.g. `notification-service:notification-event:<key>`); the segments of a key are always separated by `:`, never `_`; get/set are added only when a flow needs them.
