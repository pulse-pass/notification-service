---
description: Log format, Map argument, S5145 and what never to log
paths:
  - "src/main/java/**/*.java"
---

# Logs

- **NTF-083** Logs follow `ClassName.method - message`.
- **NTF-084** The message is literal text; data goes in a single `Map` argument (`logger.error("EmailGatewayImpl.sendEmail - Request failed {}", Map.of("failure", ex.getClass().getSimpleName()))`), never interpolated in the sentence.
- **NTF-085** The exception is passed as the last argument, never `e.getMessage()`, except in a gateway whose request carries a secret (NTF-189) and in the base `catch (Exception ex)` of the service around that gateway (NTF-081), which log only the exception class.
- **NTF-086** A log never receives a value that came straight from the Kafka message (event key, type, origin, contact, variables), which Sonar flags as log injection (`javasecurity:S5145`) and fails the quality gate.
- **NTF-087** Log the value of the loaded entity (`notificationEvent.getNotificationEventKey()`) instead, or leave it out when nothing was loaded yet.
- **NTF-088** In the application layer, every `if` that changes the flow has a log as its first line: `logger.warn` when it refuses the message, `logger.info` when it takes another path (type not handled, event already sent).
- **NTF-089** Passwords and tokens never appear in a log, and the log `Map` never includes them.
- **NTF-090** The recipient e-mail address and the variables of an e-mail are not logged; the keys of the event and of the e-mail message are.
- **NTF-091** Logs never receive a DTO, message, entity or exception payload: only the single `Map` with the chosen fields; records do not override `toString`.
