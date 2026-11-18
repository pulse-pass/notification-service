---
description: MySQL modeling conventions
paths:
  - "infrastructure/database.sql"
  - "src/main/java/**/infrastructure/jpa/**"
---

# Database

- **NTF-001** One schema per service, named after the service without a `_db` suffix (`organization`, `onboarding`, `event`, `purchase_order`, `webhook`, `notification`); the local database has the same name.
- **NTF-002** Tables and columns in snake_case and singular; compound names when a single word is too generic.
- **NTF-003** `id BIGINT AUTO_INCREMENT` as the internal PK (keep the `AUTO_INCREMENT`) and a unique `<table>_key VARCHAR(36)` (UUID) used between services.
- **NTF-004** Column order: id, public key, references, type, own data, dates, JSON, status, `created_at`/`updated_at` (`TIMESTAMP(3)`).
- **NTF-005** Types, statuses and origins (`notification_event_type`, `email_message_status`, `notification_origin`) are domain tables `<x>_type`, `<x>_status` and `<x>_origin` with an `enumerator`, seeded with `INSERT IGNORE` and referenced by FK; never free text.
- **NTF-006** `<x>_status` and `<x>_type` tables only have `created_at`; a type table may carry the configuration of that type (`email_subject`, `email_template`, `email_variables`), so a new type is a new row plus its template.
- **NTF-007** Every entity with a status has `<x>_history` (`<x>_history_key`, `<x>_id`, `old_status_id` null on creation, `new_status_id`, `history_data` JSON, only `created_at`); an entity without a status (`notification_event`) has no history.
- **NTF-008** Named constraints: `pk_<table>`, `<table>_<column>_unique`, `fkN_<table>`, `<table>_<column>_index`.
- **NTF-009** A column used as a list filter gets an index (`INDEX <table>_<column>_index (<column>)`) unless a `UNIQUE` or FK already covers it.
- **NTF-010** No relationships across schemas: reference by `<x>_key` (`person_key`, `origin_key`); the message carries a copy of everything the service needs, so it never queries another service.
- **NTF-011** No migrations: `database.sql` is idempotent (`CREATE SCHEMA/TABLE IF NOT EXISTS`, `INSERT IGNORE` on tables with a unique enumerator) and has no credentials.
- **NTF-208** The id that an external system gives to a record is stored in `external_<x>_key VARCHAR(36) NULL` after the references (`email_message.external_message_key`, the id of the e-mail at the provider, filled when the e-mail is sent).
