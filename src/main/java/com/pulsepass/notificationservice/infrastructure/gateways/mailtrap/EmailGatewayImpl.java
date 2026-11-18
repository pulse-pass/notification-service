package com.pulsepass.notificationservice.infrastructure.gateways.mailtrap;

import com.pulsepass.notificationservice.domain.exceptions.EmailProviderUnavailableException;
import com.pulsepass.notificationservice.domain.exceptions.EmailRefusedException;
import com.pulsepass.notificationservice.domain.gateways.email.EmailGateway;
import com.pulsepass.notificationservice.domain.gateways.email.dtos.SendEmailInputDTO;
import com.pulsepass.notificationservice.domain.gateways.email.dtos.SendEmailOutputDTO;
import com.pulsepass.notificationservice.infrastructure.gateways.mailtrap.dtos.MailtrapSendEmailRequest;
import com.pulsepass.notificationservice.infrastructure.gateways.mailtrap.dtos.MailtrapSendEmailResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailGatewayImpl implements EmailGateway {

    private static final Logger logger = LoggerFactory.getLogger(EmailGatewayImpl.class);

    private final TemplateEngine templateEngine;
    private final RestClient restClient;
    private final String apiToken;
    private final String sandboxId;
    private final String fromEmail;
    private final String fromName;

    public EmailGatewayImpl(
            TemplateEngine templateEngine,
            @Value("${app.mailtrap.url}") String url,
            @Value("${app.mailtrap.api-token}") String apiToken,
            @Value("${app.mailtrap.sandbox-id}") String sandboxId,
            @Value("${app.mail.from-email}") String fromEmail,
            @Value("${app.mail.from-name}") String fromName,
            RestClient.Builder restClientBuilder
    ) {
        this.templateEngine = templateEngine;
        this.restClient = restClientBuilder
                .baseUrl(url)
                .build();
        this.apiToken = apiToken;
        this.sandboxId = sandboxId;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    private String renderTemplate(String name, SendEmailInputDTO input) {
        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariables(input.variables());
        return templateEngine.process("email/" + name, context);
    }

    @Override
    public SendEmailOutputDTO sendEmail(SendEmailInputDTO input) {
        MailtrapSendEmailRequest request = new MailtrapSendEmailRequest(
                new MailtrapSendEmailRequest.Address(fromEmail, fromName),
                List.of(new MailtrapSendEmailRequest.Address(input.recipientEmail(), input.recipientName())),
                input.subject(),
                renderTemplate(input.template() + ".txt", input),
                renderTemplate(input.template() + ".html", input),
                input.template()
        );

        ResponseEntity<MailtrapSendEmailResponse> response;
        try {
            response = restClient.post()
                    .uri("/api/send/{sandboxId}", sandboxId)
                    .header("Api-Token", apiToken)
                    .header("User-Agent", "pulse-pass-notification-service")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (clientRequest, clientResponse) -> {})
                    .toEntity(MailtrapSendEmailResponse.class);
        } catch (RestClientException ex) {
            logger.error(
                    "EmailGatewayImpl.sendEmail - Request failed {}",
                    Map.of("failure", ex.getClass().getSimpleName())
            );
            throw new EmailProviderUnavailableException();
        }

        int status = response.getStatusCode().value();
        if (status == 400 || status == 422) {
            logger.warn(
                    "EmailGatewayImpl.sendEmail - Email refused by the provider {}",
                    Map.of("status", status)
            );
            throw new EmailRefusedException();
        }
        if (status != 200) {
            logger.error(
                    "EmailGatewayImpl.sendEmail - Unexpected status {}",
                    Map.of("status", status)
            );
            throw new EmailProviderUnavailableException();
        }
        MailtrapSendEmailResponse body = response.getBody();
        if (body == null) {
            logger.error(
                    "EmailGatewayImpl.sendEmail - Response without body {}",
                    Map.of("status", status)
            );
            throw new EmailProviderUnavailableException();
        }

        return new SendEmailOutputDTO(body.messageIds().get(0));
    }
}
