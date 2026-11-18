package com.pulsepass.notificationservice.application.services;

import com.pulsepass.notificationservice.application.dtos.input.ProcessNotificationEventDTO;
import com.pulsepass.notificationservice.common.JsonHandlers;
import com.pulsepass.notificationservice.common.JsonParseHandlers;
import com.pulsepass.notificationservice.domain.UnitOfWork;
import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;
import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatusEnum;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.domain.exceptions.EmailProviderUnavailableException;
import com.pulsepass.notificationservice.domain.exceptions.EmailRefusedException;
import com.pulsepass.notificationservice.domain.exceptions.EmailSendingException;
import com.pulsepass.notificationservice.domain.exceptions.InvalidNotificationEventException;
import com.pulsepass.notificationservice.domain.exceptions.NotificationEventTypeNotFoundException;
import com.pulsepass.notificationservice.domain.gateways.email.EmailGateway;
import com.pulsepass.notificationservice.domain.gateways.email.dtos.SendEmailInputDTO;
import com.pulsepass.notificationservice.domain.gateways.email.dtos.SendEmailOutputDTO;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageAttemptRepository;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageHistoryRepository;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageRepository;
import com.pulsepass.notificationservice.domain.repositories.EmailMessageStatusRepository;
import com.pulsepass.notificationservice.domain.repositories.NotificationEventRepository;
import com.pulsepass.notificationservice.domain.repositories.NotificationEventTypeRepository;
import com.pulsepass.notificationservice.domain.repositories.NotificationOriginRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventService.class);
    private static final int MAX_EMAIL_ATTEMPTS = 5;

    private final UnitOfWork unitOfWork;
    private final EmailGateway emailGateway;
    private final NotificationEventTypeRepository notificationEventTypeRepository;
    private final NotificationOriginRepository notificationOriginRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final EmailMessageStatusRepository emailMessageStatusRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailMessageAttemptRepository emailMessageAttemptRepository;
    private final EmailMessageHistoryRepository emailMessageHistoryRepository;

    public NotificationEventService(
            UnitOfWork unitOfWork,
            EmailGateway emailGateway,
            NotificationEventTypeRepository notificationEventTypeRepository,
            NotificationOriginRepository notificationOriginRepository,
            NotificationEventRepository notificationEventRepository,
            EmailMessageStatusRepository emailMessageStatusRepository,
            EmailMessageRepository emailMessageRepository,
            EmailMessageAttemptRepository emailMessageAttemptRepository,
            EmailMessageHistoryRepository emailMessageHistoryRepository
    ) {
        this.unitOfWork = unitOfWork;
        this.emailGateway = emailGateway;
        this.notificationEventTypeRepository = notificationEventTypeRepository;
        this.notificationOriginRepository = notificationOriginRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.emailMessageStatusRepository = emailMessageStatusRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.emailMessageAttemptRepository = emailMessageAttemptRepository;
        this.emailMessageHistoryRepository = emailMessageHistoryRepository;
    }

    private void validateEmailVariables(NotificationEventType notificationEventType, ProcessNotificationEventDTO dto) {
        for (String variableName : notificationEventType.getEmailVariableNames()) {
            Object variableValue = dto.emailVariables().get(variableName);
            if (variableValue == null || variableValue.toString().isBlank()) {
                logger.warn("NotificationEventService.validateEmailVariables - Required variable is missing");
                throw new InvalidNotificationEventException();
            }
        }
    }

    private void createEmailMessage(
            NotificationEvent notificationEvent,
            NotificationEventType notificationEventType,
            ProcessNotificationEventDTO dto
    ) {
        String emailData = JsonHandlers.toJson(new SendEmailInputDTO(
                dto.receiverEmail(),
                dto.receiverName(),
                notificationEventType.getEmailSubject(),
                notificationEventType.getEmailTemplate(),
                dto.emailVariables()
        ));

        EmailMessageStatus pendingStatus = emailMessageStatusRepository
                .findByEnumerator(EmailMessageStatusEnum.PENDING.name())
                .orElseThrow(() -> new IllegalStateException("Email message status PENDING not found"));

        EmailMessage emailMessage = emailMessageRepository.save(new EmailMessage(
                UUID.randomUUID().toString(),
                notificationEvent,
                emailData,
                pendingStatus
        ));
        emailMessageHistoryRepository.save(EmailMessageHistory.fromEmailMessage(emailMessage, null));
    }

    private void processEmailMessage(NotificationEvent notificationEvent) {
        EmailMessage emailMessage = emailMessageRepository
                .findByNotificationEventId(notificationEvent.getId())
                .orElseThrow(() -> new IllegalStateException("Email message not found for the event"));
        if (EmailMessageStatusEnum.SENT.name().equals(emailMessage.getEmailMessageStatus().getEnumerator())) {
            logger.info(
                    "NotificationEventService.processEmailMessage - Email already sent {}",
                    Map.of("email_message_key", emailMessage.getEmailMessageKey())
            );
            return;
        }
        if (EmailMessageStatusEnum.FAILED.name().equals(emailMessage.getEmailMessageStatus().getEnumerator())) {
            logger.info(
                    "NotificationEventService.processEmailMessage - Email already failed {}",
                    Map.of("email_message_key", emailMessage.getEmailMessageKey())
            );
            return;
        }

        SendEmailInputDTO sendEmailInput = JsonParseHandlers.fromJson(
                emailMessage.getEmailData(),
                SendEmailInputDTO.class
        );
        SendEmailOutputDTO sendEmailOutput;
        try {
            sendEmailOutput = emailGateway.sendEmail(sendEmailInput);
        } catch (EmailRefusedException ex) {
            logger.warn(
                    "NotificationEventService.processEmailMessage - Email refused by the provider {}",
                    Map.of("email_message_key", emailMessage.getEmailMessageKey()),
                    ex
            );
            emailMessageAttemptRepository.save(new EmailMessageAttempt(
                    UUID.randomUUID().toString(),
                    emailMessage,
                    ex.getCode()
            ));
            EmailMessageStatus failedStatus = emailMessageStatusRepository
                    .findByEnumerator(EmailMessageStatusEnum.FAILED.name())
                    .orElseThrow(() -> new IllegalStateException("Email message status FAILED not found"));
            EmailMessageStatus oldStatus = emailMessage.getEmailMessageStatus();
            emailMessage.setEmailMessageStatus(failedStatus);
            emailMessageRepository.save(emailMessage);
            emailMessageHistoryRepository.save(EmailMessageHistory.fromEmailMessage(emailMessage, oldStatus));
            unitOfWork.commit();
            return;
        } catch (EmailProviderUnavailableException ex) {
            logger.warn(
                    "NotificationEventService.processEmailMessage - Email provider unavailable {}",
                    Map.of("email_message_key", emailMessage.getEmailMessageKey()),
                    ex
            );
            emailMessageAttemptRepository.save(new EmailMessageAttempt(
                    UUID.randomUUID().toString(),
                    emailMessage,
                    ex.getCode()
            ));
            long attempts = emailMessageAttemptRepository.countByEmailMessageId(emailMessage.getId());
            if (attempts < MAX_EMAIL_ATTEMPTS) {
                logger.info(
                        "NotificationEventService.processEmailMessage - Email attempt failed, will be retried {}",
                        Map.of("email_message_key", emailMessage.getEmailMessageKey(), "attempts", attempts)
                );
                unitOfWork.commit();
                throw ex;
            }
            EmailMessageStatus failedStatus = emailMessageStatusRepository
                    .findByEnumerator(EmailMessageStatusEnum.FAILED.name())
                    .orElseThrow(() -> new IllegalStateException("Email message status FAILED not found"));
            EmailMessageStatus oldStatus = emailMessage.getEmailMessageStatus();
            emailMessage.setEmailMessageStatus(failedStatus);
            emailMessageRepository.save(emailMessage);
            emailMessageHistoryRepository.save(EmailMessageHistory.fromEmailMessage(emailMessage, oldStatus));
            unitOfWork.commit();
            logger.warn(
                    "NotificationEventService.processEmailMessage - Email attempts limit reached {}",
                    Map.of("email_message_key", emailMessage.getEmailMessageKey(), "attempts", attempts)
            );
            return;
        } catch (Exception ex) {
            logger.error(
                    "NotificationEventService.processEmailMessage - Email send failed {}",
                    Map.of(
                            "email_message_key", emailMessage.getEmailMessageKey(),
                            "failure", ex.getClass().getSimpleName()
                    )
            );
            emailMessageAttemptRepository.save(new EmailMessageAttempt(
                    UUID.randomUUID().toString(),
                    emailMessage,
                    new EmailSendingException().getCode()
            ));
            EmailMessageStatus failedStatus = emailMessageStatusRepository
                    .findByEnumerator(EmailMessageStatusEnum.FAILED.name())
                    .orElseThrow(() -> new IllegalStateException("Email message status FAILED not found"));
            EmailMessageStatus oldStatus = emailMessage.getEmailMessageStatus();
            emailMessage.setEmailMessageStatus(failedStatus);
            emailMessageRepository.save(emailMessage);
            emailMessageHistoryRepository.save(EmailMessageHistory.fromEmailMessage(emailMessage, oldStatus));
            unitOfWork.commit();
            return;
        }

        emailMessageAttemptRepository.save(new EmailMessageAttempt(
                UUID.randomUUID().toString(),
                emailMessage,
                null
        ));
        EmailMessageStatus sentStatus = emailMessageStatusRepository
                .findByEnumerator(EmailMessageStatusEnum.SENT.name())
                .orElseThrow(() -> new IllegalStateException("Email message status SENT not found"));
        emailMessage.setExternalMessageKey(sendEmailOutput.externalMessageKey());
        EmailMessageStatus oldStatus = emailMessage.getEmailMessageStatus();
        emailMessage.setEmailMessageStatus(sentStatus);
        emailMessageRepository.save(emailMessage);
        emailMessageHistoryRepository.save(EmailMessageHistory.fromEmailMessage(emailMessage, oldStatus));
        unitOfWork.commit();

        logger.info(
                "NotificationEventService.processEmailMessage - Email sent {}",
                Map.of("email_message_key", emailMessage.getEmailMessageKey())
        );
    }

    public void processNotificationEventCreation(ProcessNotificationEventDTO dto) {
        logger.info("NotificationEventService.processNotificationEventCreation - Starting");

        Optional<NotificationEventType> notificationEventType = notificationEventTypeRepository
                .findByEnumerator(dto.notificationEventType());
        if (notificationEventType.isEmpty()) {
            logger.warn(
                    "NotificationEventService.processNotificationEventCreation - Event type not found");
            throw new NotificationEventTypeNotFoundException();
        }

        Optional<NotificationOrigin> notificationOrigin = notificationOriginRepository.findByEnumerator(dto.origin());
        if (notificationOrigin.isEmpty()) {
            logger.warn("NotificationEventService.processNotificationEventCreation - Notification origin not found");
            throw new InvalidNotificationEventException();
        }

        validateEmailVariables(notificationEventType.get(), dto);

        unitOfWork.begin();

        Optional<NotificationEvent> existingEvent = notificationEventRepository
                .findForUpdateByNotificationEventKey(dto.notificationEventKey());
        NotificationEvent notificationEvent;
        if (existingEvent.isEmpty()) {
            logger.info(
                    "NotificationEventService.processNotificationEventCreation - Saving the new event");
            notificationEvent = notificationEventRepository.save(new NotificationEvent(
                    dto.notificationEventKey(),
                    dto.personKey(),
                    dto.originKey(),
                    notificationEventType.get(),
                    notificationOrigin.get(),
                    dto.data()
            ));
            createEmailMessage(notificationEvent, notificationEventType.get(), dto);
        } else {
            logger.info("NotificationEventService.processNotificationEventCreation - Event already exists");
            notificationEvent = existingEvent.get();
        }

        processEmailMessage(notificationEvent);
    }
}
