---
description: UnitOfWork, repositories, locking and loading
paths:
  - "**/application/**"
  - "**/domain/**"
  - "**/infrastructure/jpa/**"
  - "**/api/**"
---

# Persistence

## Transactions

- **NTF-111** Explicit transaction control through the `UnitOfWork` (`JpaUnitOfWork`, a singleton that keeps the transaction in a `ThreadLocal`), never `@Transactional`.
- **NTF-112** Reads of the public method go before `unitOfWork.begin()`, validations first (e.g. the event type and origin); `begin()` comes right after them and the transaction runs through the whole flow, gateway call included (NTF-080).
- **NTF-113** No manual rollback in services: the `OpenEntityManagerInRecordInterceptor` rolls back when the unit of work is still active (NTF-110).
- **NTF-114** Updating an existing record: the lookup of the item is `findForUpdateBy<Key>`, with the same name in the domain repository, the `*RepositoryImpl` and the `Jpa*Repository`.
- **NTF-115** In the `Jpa*Repository` it is a derived query with `@Lock(LockModeType.PESSIMISTIC_WRITE)` and no `@Query`; `ForUpdate` goes before `By` because Spring Data reads everything after `By` as criteria.
- **NTF-116** The lookup with the lock is the first call after `unitOfWork.begin()`, and it is the only lock of the flow.
- **NTF-117** The validations that depend on the item come right after it, inside the transaction (the record interceptor rolls back).
- **NTF-118** Lookups of domain tables of the public method go before `begin()`; those of a channel (the statuses of the e-mail) are made inside its private methods, which run within the transaction of the public method (NTF-044), such as the `PENDING` status in `createEmailMessage`.

## Repositories

- **NTF-119** Repositories use only derived queries (no `@Query`/JPQL, no `@EntityGraph`).
- **NTF-120** Each repository returns the entity of its own table with `toDomain()` already assembling the relationships; the service uses what the domain loaded, without fetching the same entity again.
- **NTF-121** Always take the simplest path: plain derived lookups in sequence, no custom queries, extra abstractions or redundant calls when a direct one solves it.
- **NTF-122** `*RepositoryImpl` calls the plain `jpaRepository.save` (no `saveAndFlush`). With `AUTO_INCREMENT` the INSERT runs on `save`, inside the transaction.
- **NTF-123** When updating a loaded object, call `repository.save(object)` and keep using the same object: never capture the return in a variable such as `saved` or `failed`.
- **NTF-124** The return of `save` is used only when creating, to get the generated data.
- **NTF-125** Existence checks use `findBy...().isPresent()`, never `existsBy...` methods.

## Loading

- **NTF-126** Relationships are `FetchType.LAZY`, like in the other services, and `toDomain()` needs an open Hibernate session.
- **NTF-127** The consumer has no web layer, so no open-in-view: the `OpenEntityManagerInRecordInterceptor` (`infrastructure/kafka`) keeps one `EntityManager` open per record, as the open-in-view does per request: `intercept` opens it and binds it to the `EntityManagerFactory` (`TransactionSynchronizationManager.bindResource` with an `EntityManagerHolder`), and `afterRecord` unbinds and closes it; each new attempt of the same record uses a new one.
