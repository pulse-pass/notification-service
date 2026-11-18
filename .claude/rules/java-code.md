---
description: Java code style: method order, flow, naming, ifs, catch, comments
paths:
  - "src/main/java/**/*.java"
---

# Java code

## Method order and flow

- **NTF-041** Private methods always before public ones, in the order they are used.
- **NTF-042** In services and consumers, public methods go: create, then update, then delete, then get/list.
- **NTF-043** Private methods only for real auxiliary steps used by several public methods or with a clear business name of their own.
- **NTF-044** The flow of a public method stays inline and linear, in the method itself: never extract its branches or its save, history and commit sequence to a private method (`saveAndCommit`), even when lines repeat, and never to shorten the method; the only exception is the logic of one channel, which lives in a trio of private methods called by the public flow, in the order of use: `validate<Channel>*` (checks what the channel needs), `create<Channel>Message` (looks up the statuses it needs, builds and saves the message and its history, called inside the transaction the public method opened) and `process<Channel>Message` (sends, records the attempt and sets the status, also inside that transaction, which it commits), so a new channel is another trio of methods and the public method keeps only the event.
- **NTF-045** Lookups of domain tables by enumerator (`findByEnumerator(...).orElseThrow(...)`) stay inline in the method that needs them, even when repeated, and are never extracted to a method of their own; in the private methods of a channel (NTF-044) they are made by those methods, which run inside the transaction of the public method, e.g. the `PENDING` status in `createEmailMessage` and the `SENT` and `FAILED` statuses in `processEmailMessage`; the public method never fetches them to pass down as parameters.
- **NTF-046** Prefer separate, clear `if`s over compound `||` conditions when each case has its own meaning.
- **NTF-047** One `catch` per different handling: two `catch` blocks that do the same thing become a single `catch (Exception ex)`. A separate `catch` exists only when that exception has specific handling.
- **NTF-048** `try/catch` only around the call that actually throws (e.g. the `save` hitting a `UNIQUE` → `DataIntegrityViolationException` becomes the domain duplicate exception).

## Style

- **NTF-049** No defensive validation (e.g. checking that a value from the environment is not empty).
- **NTF-050** Keep it simple: compare strings with `equals`, no `MessageDigest.isEqual` or similar tricks.
- **NTF-051** No comments in Java code (Javadoc included): names and structure must explain the code, and decisions go to the rules, the README or the task notes.

## Conversions and naming

- **NTF-052** Static factories follow `from<Source>` (`fromEntity`, `fromDomain`, `EmailMessageHistory.fromEmailMessage`), not `create`.
- **NTF-053** `to*`/`from*` methods exist only in classes that represent data, converting themselves: `toDTO()` in messages, `toDomain()` and static `fromDomain(...)` in JPA entities, `toHistoryData()` in domain entities.
- **NTF-054** Static `from*` also exist in history entities (`fromEmailMessage`), plus the static helpers of `common/` (`JsonHandlers.toJson`, `toSnakeCase`).
- **NTF-055** Services, gateways, consumers and other components never have a `to*`/`from*` method; `to*` is reserved for conversions of the object itself and static helpers, never for a private step that validates.
- **NTF-056** A private step that checks a value is `validate*` and returns nothing; one that builds something is named after the action (`fetch*`, `build*`).
- **NTF-207** Declare the explicit type for domain entities, statuses, DTOs and `Optional` (`EmailMessageStatus pendingStatus = ...`, `Optional<NotificationEvent> existingEvent = ...`, `SendEmailInputDTO sendEmailInput = ...`), as `OrganizationService.updateOrganization` does; `var` only for trivial locals (a loop item of a primitive or `String` is also typed).
