SET NAMES utf8mb4;

CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE IF NOT EXISTS notification.notification_event_type (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    enumerator       VARCHAR(50)    NOT NULL,
    email_subject    VARCHAR(255)   NOT NULL,
    email_template   VARCHAR(100)   NOT NULL,
    email_variables  VARCHAR(1056)  NOT NULL,
    created_at       TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_notification_event_type PRIMARY KEY (id),
    CONSTRAINT notification_event_type_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO notification.notification_event_type (enumerator, email_subject, email_template, email_variables) VALUES
    ('ORGANIZER_APPROVED', 'Seu cadastro no PulsePass foi aprovado',  'organizer-approved', 'recipient_name,legal_name'),
    ('ORGANIZER_REJECTED', 'Seu cadastro no PulsePass foi reprovado', 'organizer-rejected', 'recipient_name,legal_name,rejection_reason_description');

CREATE TABLE IF NOT EXISTS notification.notification_origin (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    enumerator  VARCHAR(50)   NOT NULL,
    created_at  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_notification_origin PRIMARY KEY (id),
    CONSTRAINT notification_origin_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO notification.notification_origin (enumerator)
VALUES ('ORGANIZATION');

CREATE TABLE IF NOT EXISTS notification.email_message_status (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    enumerator  VARCHAR(50)   NOT NULL,
    created_at  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_email_message_status PRIMARY KEY (id),
    CONSTRAINT email_message_status_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO notification.email_message_status (enumerator)
VALUES ('PENDING'), ('SENT'), ('FAILED');

CREATE TABLE IF NOT EXISTS notification.notification_event (
    id                          BIGINT        NOT NULL AUTO_INCREMENT,
    notification_event_key      VARCHAR(36)   NOT NULL,
    person_key                  VARCHAR(36)   NULL,
    origin_key                  VARCHAR(36)   NOT NULL,
    notification_event_type_id  BIGINT        NOT NULL,
    notification_origin_id      BIGINT        NOT NULL,
    data                        JSON          NOT NULL,
    created_at                  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at                  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_notification_event PRIMARY KEY (id),
    CONSTRAINT notification_event_key_unique UNIQUE (notification_event_key),
    CONSTRAINT fk1_notification_event FOREIGN KEY (notification_event_type_id)
        REFERENCES notification.notification_event_type (id),
    CONSTRAINT fk2_notification_event FOREIGN KEY (notification_origin_id)
        REFERENCES notification.notification_origin (id)
);

CREATE TABLE IF NOT EXISTS notification.email_message (
    id                        BIGINT        NOT NULL AUTO_INCREMENT,
    email_message_key         VARCHAR(36)   NOT NULL,
    notification_event_id     BIGINT        NOT NULL,
    external_message_key      VARCHAR(36)   NULL,
    email_data                JSON          NOT NULL,
    email_message_status_id   BIGINT        NOT NULL,
    created_at                TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at                TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_email_message PRIMARY KEY (id),
    CONSTRAINT email_message_key_unique UNIQUE (email_message_key),
    CONSTRAINT fk1_email_message FOREIGN KEY (notification_event_id)
        REFERENCES notification.notification_event (id),
    CONSTRAINT fk2_email_message FOREIGN KEY (email_message_status_id)
        REFERENCES notification.email_message_status (id)
);

CREATE TABLE IF NOT EXISTS notification.email_message_attempt (
    id                         BIGINT         NOT NULL AUTO_INCREMENT,
    email_message_attempt_key  VARCHAR(36)    NOT NULL,
    email_message_id           BIGINT         NOT NULL,
    error_code                 VARCHAR(8)     NULL,
    created_at                 TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at                 TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_email_message_attempt PRIMARY KEY (id),
    CONSTRAINT email_message_attempt_key_unique UNIQUE (email_message_attempt_key),
    CONSTRAINT fk1_email_message_attempt FOREIGN KEY (email_message_id)
        REFERENCES notification.email_message (id)
);

CREATE TABLE IF NOT EXISTS notification.email_message_history (
    id                         BIGINT        NOT NULL AUTO_INCREMENT,
    email_message_history_key  VARCHAR(36)   NOT NULL,
    email_message_id           BIGINT        NOT NULL,
    old_status_id              BIGINT        NULL,
    new_status_id              BIGINT        NOT NULL,
    history_data               JSON          NOT NULL,
    created_at                 TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_email_message_history PRIMARY KEY (id),
    CONSTRAINT email_message_history_key_unique UNIQUE (email_message_history_key),
    CONSTRAINT fk1_email_message_history FOREIGN KEY (email_message_id)
        REFERENCES notification.email_message (id),
    CONSTRAINT fk2_email_message_history FOREIGN KEY (old_status_id)
        REFERENCES notification.email_message_status (id),
    CONSTRAINT fk3_email_message_history FOREIGN KEY (new_status_id)
        REFERENCES notification.email_message_status (id)
);
