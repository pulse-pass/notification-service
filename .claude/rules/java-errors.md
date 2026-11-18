---
description: Errors: DomainException, codes and failure handling without HTTP
paths:
  - "**/exceptions/**"
  - "**/application/**"
  - "**/api/**"
---

# Errors

- **NTF-057** The service has no HTTP API, so there is no error handler, response or `messages*.properties`: a mapped error is caught in the service and handled there; any other exception leaves the service method and sends the message to the retry topic, tried again every `retry.delay` up to the 7-day limit (NTF-186); the service also catches any other failure of the e-mail gateway with `catch (Exception ex)`, records the attempt with the code of `EmailSendingException`, sets `FAILED` and does not rethrow (NTF-081).
- **NTF-058** Business exceptions extend `DomainException` (`getCode()`, `getMessageKey()`) and have a constructor without parameters, a literal message in `super(...)` and no fields (they never carry data, such as the status or the response of the provider), carry the service code `NTF0000N` and the message key `error.notification.<name_in_snake_case>` (`error.notification.email_refused`, `error.notification.invalid_notification_event`).
- **NTF-059** Each exception is either mapped or not: `EmailRefusedException` (code `NTF00001`, the e-mail provider refused the e-mail: status 400 or 422) is mapped and caught in the service, with no rethrow; `EmailProviderUnavailableException` (code `NTF00006`, any other status, a response without body or a transport failure) is caught in the service to record the attempt and is rethrown while the limit of NTF-204 is not reached; `EmailSendingException` (code `NTF00007`, any other failure of the gateway call, never thrown by the gateway) is created by the service in the base `catch (Exception ex)` only to take its code: the attempt is recorded, the e-mail is `FAILED` and there is no rethrow; `InvalidNotificationEventException`, `NotificationEventTypeNotFoundException`, `UnsupportedNotificationEventMessageTypeException` and `NotificationEventLockedException` are not handled and send the message to the retry topic. The provider status is handled only by the gateway (NTF-076) and the attempt records the code of the exception (`getCode()`) in `error_code`.
- **NTF-060** An unexpected failure that is not an e-mail send error (database, broker, invalid stored data) is not wrapped: it propagates as it is and the message goes to the retry topic, tried again up to the 7-day limit; a failure of the e-mail provider (unexpected status, no body, unreachable) is translated by the gateway into `EmailProviderUnavailableException` (NTF-076), and any other failure of the gateway call is handled by the `catch (Exception ex)` of the service (NTF-078, NTF-081).
- **NTF-061** Never branch on types with `instanceof` (including pattern matching); write one method per type instead.
- **NTF-062** Application setup errors (e.g. a domain table enumerator not found) throw a generic exception (`IllegalStateException`).
- **NTF-063** Exception messages are plain literal text (e.g. `"Email message status PENDING not found"`), no string concatenation.
