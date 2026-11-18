package com.pulsepass.notificationservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.application.dtos.input.ProcessNotificationEventDTO;
import com.pulsepass.notificationservice.application.services.NotificationEventService;
import com.pulsepass.notificationservice.common.JsonHandlers;
import com.pulsepass.notificationservice.domain.UnitOfWork;
import com.pulsepass.notificationservice.domain.entities.EmailMessage;
import com.pulsepass.notificationservice.domain.entities.EmailMessageAttempt;
import com.pulsepass.notificationservice.domain.entities.EmailMessageHistory;
import com.pulsepass.notificationservice.domain.entities.EmailMessageStatus;
import com.pulsepass.notificationservice.domain.entities.NotificationEvent;
import com.pulsepass.notificationservice.domain.entities.NotificationEventType;
import com.pulsepass.notificationservice.domain.entities.NotificationOrigin;
import com.pulsepass.notificationservice.domain.exceptions.EmailProviderUnavailableException;
import com.pulsepass.notificationservice.domain.exceptions.EmailRefusedException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationEventServiceTest {

    private UnitOfWork unitOfWork;
    private EmailGateway emailGateway;
    private NotificationEventTypeRepository typeRepository;
    private NotificationOriginRepository originRepository;
    private NotificationEventRepository eventRepository;
    private EmailMessageStatusRepository statusRepository;
    private EmailMessageRepository emailMessageRepository;
    private EmailMessageAttemptRepository attemptRepository;
    private EmailMessageHistoryRepository historyRepository;
    private NotificationEventService service;

    private NotificationEventType approvedType;
    private NotificationOrigin origin;
    private EmailMessageStatus pending;
    private EmailMessageStatus sent;
    private EmailMessageStatus failed;
    private ProcessNotificationEventDTO dto;

    @BeforeEach
    void setUp() {
        unitOfWork = mock(UnitOfWork.class);
        emailGateway = mock(EmailGateway.class);
        typeRepository = mock(NotificationEventTypeRepository.class);
        originRepository = mock(NotificationOriginRepository.class);
        eventRepository = mock(NotificationEventRepository.class);
        statusRepository = mock(EmailMessageStatusRepository.class);
        emailMessageRepository = mock(EmailMessageRepository.class);
        attemptRepository = mock(EmailMessageAttemptRepository.class);
        historyRepository = mock(EmailMessageHistoryRepository.class);
        service = new NotificationEventService(
                unitOfWork, emailGateway, typeRepository, originRepository, eventRepository,
                statusRepository, emailMessageRepository, attemptRepository, historyRepository);

        approvedType = new NotificationEventType(
                1L, "ORGANIZER_APPROVED", "Seu cadastro no PulsePass foi aprovado", "organizer-approved",
                "recipient_name,legal_name", null);
        origin = new NotificationOrigin(1L, "ORGANIZATION", null);
        pending = new EmailMessageStatus(1L, "PENDING", null);
        sent = new EmailMessageStatus(2L, "SENT", null);
        failed = new EmailMessageStatus(3L, "FAILED", null);
        dto = new ProcessNotificationEventDTO(
                "ORGANIZER_APPROVED", "event-key", "person-key", "ORGANIZATION", "origin-key",
                "Maria Silva", "maria@empresa.com",
                Map.of("recipient_name", "Maria Silva", "legal_name", "Eventos Silva LTDA"), "{\"origin\":1}");

        when(typeRepository.findByEnumerator("ORGANIZER_APPROVED")).thenReturn(Optional.of(approvedType));
        when(originRepository.findByEnumerator("ORGANIZATION")).thenReturn(Optional.of(origin));
        when(statusRepository.findByEnumerator("PENDING")).thenReturn(Optional.of(pending));
        when(statusRepository.findByEnumerator("SENT")).thenReturn(Optional.of(sent));
        when(statusRepository.findByEnumerator("FAILED")).thenReturn(Optional.of(failed));
        when(eventRepository.save(any(NotificationEvent.class))).thenAnswer(call -> {
            NotificationEvent event = call.getArgument(0);
            event.setId(10L);
            return event;
        });
        when(emailMessageRepository.save(any(EmailMessage.class))).thenAnswer(call -> call.getArgument(0));
        when(emailMessageRepository.findByNotificationEventId(10L))
                .thenAnswer(call -> Optional.of(storedMessage(pending)));
    }

    private String emailData() {
        return JsonHandlers.toJson(new SendEmailInputDTO(
                "maria@empresa.com", "Maria Silva", "Seu cadastro no PulsePass foi aprovado", "organizer-approved",
                Map.of("recipient_name", "Maria Silva", "legal_name", "Eventos Silva LTDA")));
    }

    private EmailMessage storedMessage(EmailMessageStatus status) {
        var event = new NotificationEvent("event-key", "person-key", "origin-key", approvedType, origin, "{}");
        event.setId(10L);
        var message = new EmailMessage("message-key", event, emailData(), status);
        message.setId(20L);
        return message;
    }

    @Test
    void processNotificationEvent_newEvent_savesPendingSendsAndMarksSent() {
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.empty());
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class))).thenReturn(new SendEmailOutputDTO("provider-id"));

        service.processNotificationEventCreation(dto);

        var eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getNotificationEventKey()).isEqualTo("event-key");
        assertThat(eventCaptor.getValue().getPersonKey()).isEqualTo("person-key");
        assertThat(eventCaptor.getValue().getOriginKey()).isEqualTo("origin-key");
        assertThat(eventCaptor.getValue().getData()).isEqualTo("{\"origin\":1}");

        var inputCaptor = ArgumentCaptor.forClass(SendEmailInputDTO.class);
        verify(emailGateway).sendEmail(inputCaptor.capture());
        assertThat(inputCaptor.getValue().recipientEmail()).isEqualTo("maria@empresa.com");
        assertThat(inputCaptor.getValue().recipientName()).isEqualTo("Maria Silva");
        assertThat(inputCaptor.getValue().subject()).isEqualTo("Seu cadastro no PulsePass foi aprovado");
        assertThat(inputCaptor.getValue().template()).isEqualTo("organizer-approved");
        assertThat(inputCaptor.getValue().variables()).containsEntry("recipient_name", "Maria Silva")
                .containsEntry("legal_name", "Eventos Silva LTDA");

        var attemptCaptor = ArgumentCaptor.forClass(EmailMessageAttempt.class);
        verify(attemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getErrorCode()).isNull();

        var historyCaptor = ArgumentCaptor.forClass(EmailMessageHistory.class);
        verify(historyRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues().get(0).getOldStatus()).isNull();
        assertThat(historyCaptor.getAllValues().get(0).getNewStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(historyCaptor.getAllValues().get(1).getOldStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(historyCaptor.getAllValues().get(1).getNewStatus().getEnumerator()).isEqualTo("SENT");

        var order = inOrder(unitOfWork, eventRepository, emailMessageRepository, emailGateway);
        order.verify(unitOfWork).begin();
        order.verify(eventRepository).findForUpdateByNotificationEventKey("event-key");
        order.verify(eventRepository).save(any(NotificationEvent.class));
        order.verify(emailGateway).sendEmail(any(SendEmailInputDTO.class));
        order.verify(unitOfWork).commit();
        verify(unitOfWork, times(1)).begin();
        verify(unitOfWork, times(1)).commit();
    }

    @Test
    void processNotificationEvent_newEvent_storesEmailDataForRetry() {
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.empty());
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class))).thenReturn(new SendEmailOutputDTO("provider-id"));

        service.processNotificationEventCreation(dto);

        var messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailMessageRepository, times(2)).save(messageCaptor.capture());
        var created = messageCaptor.getAllValues().get(0);
        assertThat(created.getEmailMessageStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(created.getEmailData()).contains("\"recipient_email\":\"maria@empresa.com\"")
                .contains("\"recipient_name\":\"Maria Silva\"")
                .contains("\"template\":\"organizer-approved\"")
                .contains("\"legal_name\":\"Eventos Silva LTDA\"");
        var updated = messageCaptor.getAllValues().get(1);
        assertThat(updated.getEmailMessageStatus().getEnumerator()).isEqualTo("SENT");
        assertThat(updated.getExternalMessageKey()).isEqualTo("provider-id");
    }

    @Test
    void processNotificationEvent_alreadySentEvent_isIgnored() {
        var event = new NotificationEvent();
        event.setId(10L);
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.of(event));
        when(emailMessageRepository.findByNotificationEventId(10L)).thenReturn(Optional.of(storedMessage(sent)));

        service.processNotificationEventCreation(dto);

        verifyNoInteractions(emailGateway, attemptRepository, historyRepository);
        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(eventRepository, never()).save(any());
    }

    @Test
    void processNotificationEvent_pendingEvent_resendsFromStoredDataWithoutCreatingEvent() {
        var event = new NotificationEvent();
        event.setId(10L);
        var message = storedMessage(pending);
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.of(event));
        when(emailMessageRepository.findByNotificationEventId(10L)).thenReturn(Optional.of(message));
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class))).thenReturn(new SendEmailOutputDTO("provider-id"));

        service.processNotificationEventCreation(dto);

        var inputCaptor = ArgumentCaptor.forClass(SendEmailInputDTO.class);
        verify(emailGateway).sendEmail(inputCaptor.capture());
        assertThat(inputCaptor.getValue().recipientEmail()).isEqualTo("maria@empresa.com");
        assertThat(inputCaptor.getValue().variables())
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of("recipient_name", "Maria Silva", "legal_name", "Eventos Silva LTDA"));
        verify(eventRepository, never()).save(any());
        verify(attemptRepository).save(any(EmailMessageAttempt.class));
        assertThat(message.getEmailMessageStatus().getEnumerator()).isEqualTo("SENT");
        assertThat(message.getExternalMessageKey()).isEqualTo("provider-id");
    }

    @Test
    void processNotificationEvent_failedEvent_isIgnored() {
        var event = new NotificationEvent();
        event.setId(10L);
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.of(event));
        when(emailMessageRepository.findByNotificationEventId(10L)).thenReturn(Optional.of(storedMessage(failed)));

        service.processNotificationEventCreation(dto);

        verifyNoInteractions(emailGateway, attemptRepository, historyRepository);
        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(eventRepository, never()).save(any());
    }

    @Test
    void processNotificationEvent_deliveryRefusal_savesAttemptMarksFailedAndDoesNotRethrow() {
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.empty());
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class)))
                .thenThrow(new EmailRefusedException());
        var message = storedMessage(pending);
        when(emailMessageRepository.findByNotificationEventId(10L)).thenReturn(Optional.of(message));

        service.processNotificationEventCreation(dto);

        var attemptCaptor = ArgumentCaptor.forClass(EmailMessageAttempt.class);
        verify(attemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getErrorCode()).isEqualTo("NTF00001");
        assertThat(message.getEmailMessageStatus().getEnumerator()).isEqualTo("FAILED");
        var historyCaptor = ArgumentCaptor.forClass(EmailMessageHistory.class);
        verify(historyRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues().get(1).getOldStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(historyCaptor.getAllValues().get(1).getNewStatus().getEnumerator()).isEqualTo("FAILED");
        verify(unitOfWork).begin();
        verify(unitOfWork).commit();
    }

    @Test
    void processNotificationEvent_unknownEventType_throwsTypeNotFoundWithoutWriting() {
        when(typeRepository.findByEnumerator("TICKET_ISSUED")).thenReturn(Optional.empty());
        var unhandled = new ProcessNotificationEventDTO(
                "TICKET_ISSUED", "event-key", null, "ORGANIZATION", "origin-key",
                "Maria", "maria@empresa.com", Map.of(), "{}");

        assertThatThrownBy(() -> service.processNotificationEventCreation(unhandled))
                .isInstanceOf(NotificationEventTypeNotFoundException.class);

        verifyNoInteractions(eventRepository, emailGateway, unitOfWork, attemptRepository, historyRepository);
    }

    @Test
    void processNotificationEvent_unknownOrigin_throwsInvalidEvent() {
        when(originRepository.findByEnumerator("UNKNOWN")).thenReturn(Optional.empty());
        var invalid = new ProcessNotificationEventDTO(
                "ORGANIZER_APPROVED", "event-key", null, "UNKNOWN", "origin-key",
                "Maria", "maria@empresa.com", Map.of("legal_name", "x"), "{}");

        assertThatThrownBy(() -> service.processNotificationEventCreation(invalid))
                .isInstanceOf(InvalidNotificationEventException.class);

        verifyNoInteractions(eventRepository, emailGateway, unitOfWork);
    }

    @Test
    void processNotificationEvent_missingRecipientNameVariable_throwsInvalidEvent() {
        var invalid = new ProcessNotificationEventDTO(
                "ORGANIZER_APPROVED", "event-key", null, "ORGANIZATION", "origin-key",
                "Maria", "maria@empresa.com", Map.of("legal_name", "x"), "{}");

        assertThatThrownBy(() -> service.processNotificationEventCreation(invalid))
                .isInstanceOf(InvalidNotificationEventException.class);

        verifyNoInteractions(eventRepository, emailGateway, unitOfWork);
    }

    @Test
    void processNotificationEvent_missingRequiredVariable_throwsInvalidEvent() {
        var rejectedType = new NotificationEventType(
                2L, "ORGANIZER_REJECTED", "subject", "organizer-rejected",
                "recipient_name,legal_name,rejection_reason_description", null);
        when(typeRepository.findByEnumerator("ORGANIZER_REJECTED")).thenReturn(Optional.of(rejectedType));
        var invalid = new ProcessNotificationEventDTO(
                "ORGANIZER_REJECTED", "event-key", null, "ORGANIZATION", "origin-key",
                "Maria", "maria@empresa.com", Map.of("legal_name", "x"), "{}");

        assertThatThrownBy(() -> service.processNotificationEventCreation(invalid))
                .isInstanceOf(InvalidNotificationEventException.class);

        verifyNoInteractions(eventRepository, emailGateway, unitOfWork);
    }

    @Test
    void processNotificationEvent_providerUnavailableBelowLimit_savesAttemptKeepsPendingAndRethrows() {
        for (long attempts = 1; attempts <= 4; attempts++) {
            var localAttemptRepository = mock(EmailMessageAttemptRepository.class);
            var localHistoryRepository = mock(EmailMessageHistoryRepository.class);
            var localUnitOfWork = mock(UnitOfWork.class);
            final var localService = new NotificationEventService(
                    localUnitOfWork, emailGateway, typeRepository, originRepository, eventRepository,
                    statusRepository, emailMessageRepository, localAttemptRepository, localHistoryRepository);
            var event = new NotificationEvent();
            event.setId(10L);
            var message = storedMessage(pending);
            when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.of(event));
            when(emailMessageRepository.findByNotificationEventId(10L)).thenReturn(Optional.of(message));
            when(emailGateway.sendEmail(any(SendEmailInputDTO.class)))
                    .thenThrow(new EmailProviderUnavailableException());
            when(localAttemptRepository.countByEmailMessageId(20L)).thenReturn(attempts);

            assertThatThrownBy(() -> localService.processNotificationEventCreation(dto))
                    .isInstanceOf(EmailProviderUnavailableException.class);

            var attemptCaptor = ArgumentCaptor.forClass(EmailMessageAttempt.class);
            verify(localAttemptRepository).save(attemptCaptor.capture());
            assertThat(attemptCaptor.getValue().getErrorCode()).isEqualTo("NTF00006");
            assertThat(message.getEmailMessageStatus().getEnumerator()).isEqualTo("PENDING");
            verifyNoInteractions(localHistoryRepository);
            verify(localUnitOfWork).begin();
            verify(localUnitOfWork).commit();
        }
    }

    @Test
    void processNotificationEvent_providerUnavailableAtLimit_savesAttemptMarksFailedAndDoesNotRethrow() {
        var event = new NotificationEvent();
        event.setId(10L);
        var message = storedMessage(pending);
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.of(event));
        when(emailMessageRepository.findByNotificationEventId(10L)).thenReturn(Optional.of(message));
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class)))
                .thenThrow(new EmailProviderUnavailableException());
        when(attemptRepository.countByEmailMessageId(20L)).thenReturn(5L);

        service.processNotificationEventCreation(dto);

        var attemptCaptor = ArgumentCaptor.forClass(EmailMessageAttempt.class);
        verify(attemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getErrorCode()).isEqualTo("NTF00006");
        assertThat(message.getEmailMessageStatus().getEnumerator()).isEqualTo("FAILED");
        var historyCaptor = ArgumentCaptor.forClass(EmailMessageHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getOldStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(historyCaptor.getValue().getNewStatus().getEnumerator()).isEqualTo("FAILED");
        verify(unitOfWork).begin();
        verify(unitOfWork).commit();
    }

    @Test
    void processNotificationEvent_gatewayUnexpectedFailure_savesSendingAttemptMarksFailedAndDoesNotRethrow() {
        var event = new NotificationEvent();
        event.setId(10L);
        var message = storedMessage(pending);
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.of(event));
        when(emailMessageRepository.findByNotificationEventId(10L)).thenReturn(Optional.of(message));
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class))).thenThrow(new IllegalStateException("template"));

        service.processNotificationEventCreation(dto);

        var attemptCaptor = ArgumentCaptor.forClass(EmailMessageAttempt.class);
        verify(attemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getErrorCode()).isEqualTo("NTF00007");
        assertThat(message.getEmailMessageStatus().getEnumerator()).isEqualTo("FAILED");
        var historyCaptor = ArgumentCaptor.forClass(EmailMessageHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getNewStatus().getEnumerator()).isEqualTo("FAILED");
        verify(attemptRepository, never()).countByEmailMessageId(any());
        verify(unitOfWork).commit();
    }

    @Test
    void processNotificationEvent_blankRequiredVariable_throwsInvalidEvent() {
        var invalid = new ProcessNotificationEventDTO(
                "ORGANIZER_APPROVED", "event-key", null, "ORGANIZATION", "origin-key",
                "Maria", "maria@empresa.com", Map.of("recipient_name", "  ", "legal_name", "x"), "{}");

        assertThatThrownBy(() -> service.processNotificationEventCreation(invalid))
                .isInstanceOf(InvalidNotificationEventException.class);

        verifyNoInteractions(eventRepository, emailGateway, unitOfWork);
    }

    @Test
    void processNotificationEvent_nullRequiredVariable_throwsInvalidEvent() {
        var variables = new HashMap<String, Object>();
        variables.put("recipient_name", null);
        variables.put("legal_name", "x");
        var invalid = new ProcessNotificationEventDTO(
                "ORGANIZER_APPROVED", "event-key", null, "ORGANIZATION", "origin-key",
                "Maria", "maria@empresa.com", variables, "{}");

        assertThatThrownBy(() -> service.processNotificationEventCreation(invalid))
                .isInstanceOf(InvalidNotificationEventException.class);

        verifyNoInteractions(eventRepository, emailGateway, unitOfWork);
    }

    @Test
    void processNotificationEvent_newEvent_locksEventFirstAndLooksUpEachStatusWhereItIsUsed() {
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.empty());
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class))).thenReturn(new SendEmailOutputDTO("provider-id"));

        service.processNotificationEventCreation(dto);

        var order = inOrder(unitOfWork, eventRepository, statusRepository, emailGateway);
        order.verify(unitOfWork).begin();
        order.verify(eventRepository).findForUpdateByNotificationEventKey("event-key");
        order.verify(statusRepository).findByEnumerator("PENDING");
        order.verify(emailGateway).sendEmail(any(SendEmailInputDTO.class));
        order.verify(statusRepository).findByEnumerator("SENT");
        order.verify(unitOfWork).commit();
        verify(statusRepository, never()).findByEnumerator("FAILED");
    }

    @Test
    void processNotificationEvent_pendingStatusMissing_throwsIllegalStateWithoutCommit() {
        when(statusRepository.findByEnumerator("PENDING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processNotificationEventCreation(dto))
                .isInstanceOf(IllegalStateException.class);

        verify(unitOfWork, never()).commit();
        verifyNoInteractions(emailGateway);
    }

    @Test
    void processNotificationEvent_sentStatusMissing_throwsIllegalStateWithoutCommit() {
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.empty());
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class))).thenReturn(new SendEmailOutputDTO("provider-id"));
        when(statusRepository.findByEnumerator("SENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processNotificationEventCreation(dto))
                .isInstanceOf(IllegalStateException.class);

        verify(unitOfWork, never()).commit();
    }

    @Test
    void processNotificationEvent_failedStatusMissing_throwsIllegalStateWithoutCommit() {
        when(eventRepository.findForUpdateByNotificationEventKey("event-key")).thenReturn(Optional.empty());
        when(emailGateway.sendEmail(any(SendEmailInputDTO.class))).thenThrow(new EmailRefusedException());
        when(statusRepository.findByEnumerator("FAILED")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processNotificationEventCreation(dto))
                .isInstanceOf(IllegalStateException.class);

        verify(unitOfWork, never()).commit();
    }
}
