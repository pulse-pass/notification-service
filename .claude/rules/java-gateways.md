---
description: Gateways: structure, RestClient, DTOs, failures and secrets
paths:
  - "**/domain/gateways/**"
  - "**/infrastructure/gateways/**"
  - "**/application/**"
  - "**/domain/exceptions/**"
  - "src/main/resources/application*.properties"
---

# Gateways

## Structure and naming

- **NTF-064** Nothing outside `infrastructure/` carries the name of an external system, and domain exceptions describe the capability (`EmailRefusedException`), never who implements it.
- **NTF-065** One gateway per external system, not per resource: all calls to a system go through the same class (`EmailGateway` for the e-mail provider); Redis is not a gateway but infrastructure of the service (NTF-184).
- **NTF-066** The domain interface is named after the role (`EmailGateway`).
- **NTF-067** Each gateway has its own folder in `domain/gateways/<gateway>/` (named after the gateway, not the system), holding the interface and, when it has operations with data, a `dtos/` folder (e.g. `email/EmailGateway`, `email/dtos/SendEmailInputDTO`).
- **NTF-068** The interface receives and returns those DTOs, named after the operation (`SendEmailInputDTO`, `SendEmailOutputDTO`), never loose parameters; an operation with a single simple value may take it directly.
- **NTF-069** The implementation lives in `infrastructure/gateways/<system>/`, is named after the domain interface with the `Impl` suffix, like the `*RepositoryImpl` (e.g. `mailtrap/EmailGatewayImpl`), so the provider appears only in the folder; one class per system, using `RestClient`.

## Configuration

- **NTF-070** The Mailtrap Email Sandbox API comes from `app.mailtrap.url` (`MAILTRAP_URL`, `MAILTRAP_TEST_URL` pointing to the MockServer in `test` mode), the token from `app.mailtrap.api-token` (`MAILTRAP_API_TOKEN`), the sandbox from `app.mailtrap.sandbox-id` (`MAILTRAP_SANDBOX_ID`) and the sender from `app.mail.from-email` and `app.mail.from-name` (`MAIL_FROM_EMAIL`, `MAIL_FROM_NAME`); the gateway renders the templates and posts to `POST /api/send/{sandbox_id}` with the `Api-Token` and `User-Agent` headers, and the service never touches HTTP classes.
- **NTF-071** Email bodies are Thymeleaf templates in `resources/templates/email/`, an `.html` and a `.txt` per template, named after the `email_template` of the notification type; they use only the variables sent by the producer, and the fixed URLs of the fronts (e.g. `http://localhost:3000` for the backoffice) are written in the template (NTF-205).
- **NTF-187** The service has the `spring-boot-starter-restclient` dependency (Spring Boot 4 does not auto-configure `RestClient.Builder` without it), the gateway receives the builder injected and never builds a request factory by hand, and the timeouts come from `spring.http.clients.connect-timeout` and `spring.http.clients.read-timeout`.
- **NTF-188** Request and response bodies are typed records in `infrastructure/gateways/<provider>/dtos` (`MailtrapSendEmailRequest` with its nested `Address`, `MailtrapSendEmailResponse`) with `@JsonProperty` on every field, which wins over `spring.jackson.property-naming-strategy`; the response carries `@JsonIgnoreProperties(ignoreUnknown = true)` and only the fields the service uses; no `Map` bodies.
- **NTF-072** Redis is not a gateway: it is reached only through `StringRedisTemplate` inside `RedisCache` (NTF-184), with the URL from `spring.data.redis.url`.

## Calls and failures

- **NTF-073** Each public operation of a gateway makes its own main call inline (build, `try`, result checks, mapping of the response), even when that duplicates code across operations.
- **NTF-074** Private methods are only for auxiliary steps that are not the operation itself (rendering a template, building a session).
- **NTF-075** A gateway trusts the contract of the system it calls: it checks the transport failure and the reply, and nothing else.
- **NTF-076** The email gateway reads the status and the body of the provider (the request does not throw on an error status), and the status is handled only there: `200` returns `SendEmailOutputDTO(<external message key>)`, the only item of `message_ids` (one recipient per e-mail, so one id); `400` and `422` (the provider refused the e-mail: recipient or fields) are mapped errors and become an `EmailRefusedException`, checked before the body, so a refusal without body is still a refusal; any other status (`401`, `403`, `404`, `429`, `5xx`), a `200` without body and a transport failure (`RestClientException`) are not mapped: the gateway logs the status or the class of the failure (never a secret) and throws `EmailProviderUnavailableException`; both exceptions have no fields (NTF-058); the service records the attempt with the code of the exception and the message goes to the retry topic until the limit of NTF-204.
- **NTF-189** The request of the gateway carries the token in a header, so it never logs the response body nor passes the exception to the logger or as the cause: it logs only the `Map` with the HTTP status or the exception class name; the base `catch (Exception ex)` of the service around this gateway follows the same rule.
- **NTF-077** Each `try` wraps only the call (one `try` per call); checks come after it.
- **NTF-078** Every gateway call in a service is inside a `try` with a `catch (Exception ex)`: any failure of the call is logged and becomes the domain exception of the operation, so it never leaves as a 500.
- **NTF-079** A domain exception with a different meaning gets its own `catch` before it and is rethrown, unless that `catch` handles the failure and ends the flow (the e-mail send, NTF-081).
- **NTF-080** A flow that calls a gateway inside the transaction (`begin()`/`commit()`) calls it after the lock and the validations.
- **NTF-081** The e-mail send has three `catch` blocks around the gateway call, each with its own handling (NTF-047): `catch (EmailRefusedException ex)` (a mapped error) saves the attempt with `ex.getCode()`, sets `FAILED` with its history, commits and does not rethrow; `catch (EmailProviderUnavailableException ex)` saves the attempt, counts the attempts and applies NTF-204 (retry below 5, `FAILED` on the fifth); the base `catch (Exception ex)` (NTF-078) logs the failure class with `logger.error`, saves the attempt with the code of `EmailSendingException` (`NTF00007`), sets `FAILED` with its history, commits and does not rethrow, with no retry; `EmailSendingException` is the domain exception of NTF-078 for the e-mail send, used only for its code and never thrown.
- **NTF-082** When nothing was written before the failure, the exception just propagates and the record interceptor rolls back when the record ends (NTF-110).
