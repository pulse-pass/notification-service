package com.pulsepass.notificationservice.unit.infrastructure.gateways.mailtrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.notificationservice.domain.exceptions.EmailProviderUnavailableException;
import com.pulsepass.notificationservice.domain.exceptions.EmailRefusedException;
import com.pulsepass.notificationservice.domain.gateways.email.dtos.SendEmailInputDTO;
import com.pulsepass.notificationservice.infrastructure.gateways.mailtrap.EmailGatewayImpl;
import com.pulsepass.notificationservice.infrastructure.gateways.mailtrap.EmailTemplateConfig;
import com.pulsepass.notificationservice.unit.infrastructure.gateways.StubHttpServer;
import com.pulsepass.notificationservice.unit.infrastructure.gateways.StubHttpServer.Response;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.thymeleaf.exceptions.TemplateInputException;
import tools.jackson.databind.json.JsonMapper;

class EmailGatewayImplTest {

    private static final String SANDBOX_ID = "1234567";
    private static final String PATH = "/api/send/" + SANDBOX_ID;
    private static final String API_TOKEN = "gateway-api-token";

    private final JsonMapper mapper = JsonMapper.builder().build();
    private StubHttpServer server;
    private EmailGatewayImpl gateway;

    @BeforeEach
    void setUp() throws Exception {
        server = new StubHttpServer();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(300));
        gateway = new EmailGatewayImpl(
                new EmailTemplateConfig().emailTemplateEngine(),
                server.url(),
                API_TOKEN,
                SANDBOX_ID,
                "nao-responda@pulsepass.local",
                "PulsePass",
                RestClient.builder().requestFactory(requestFactory));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    private SendEmailInputDTO approvedInput() {
        return new SendEmailInputDTO(
                "maria@empresa.com",
                "Maria Silva",
                "Seu cadastro no PulsePass foi aprovado",
                "organizer-approved",
                Map.of("recipient_name", "Maria Silva", "legal_name", "Eventos Silva LTDA"));
    }

    @Test
    void sendEmail_ok_returnsExternalMessageKey() {
        server.on("POST", PATH, Response.of(200, "{\"success\":true,\"message_ids\":[\"id-1\"]}"));

        var output = gateway.sendEmail(approvedInput());

        assertThat(output.externalMessageKey()).isEqualTo("id-1");
    }

    @Test
    void sendEmail_ok_sendsTokenHeaderSandboxPathAndRenderedBody() throws Exception {
        server.on("POST", PATH, Response.of(200, "{\"success\":true,\"message_ids\":[\"id-1\"]}"));

        gateway.sendEmail(approvedInput());

        assertThat(server.received()).hasSize(1);
        var request = server.received().get(0);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PATH);
        assertThat(request.headers().get("api-token")).isEqualTo(API_TOKEN);
        assertThat(request.headers().get("user-agent")).isEqualTo("pulse-pass-notification-service");
        assertThat(request.headers().get("content-type")).startsWith("application/json");
        var body = mapper.readTree(request.body());
        assertThat(body.propertyNames())
                .containsExactlyInAnyOrder("from", "to", "subject", "text", "html", "category");
        assertThat(body.get("from").get("email").asString()).isEqualTo("nao-responda@pulsepass.local");
        assertThat(body.get("from").get("name").asString()).isEqualTo("PulsePass");
        assertThat(body.get("to")).hasSize(1);
        assertThat(body.get("to").get(0).get("email").asString()).isEqualTo("maria@empresa.com");
        assertThat(body.get("to").get(0).get("name").asString()).isEqualTo("Maria Silva");
        assertThat(body.get("subject").asString()).isEqualTo("Seu cadastro no PulsePass foi aprovado");
        assertThat(body.get("category").asString()).isEqualTo("organizer-approved");
        assertThat(body.get("text").asString())
                .contains("Olá, Maria Silva")
                .contains("Eventos Silva LTDA")
                .contains("http://localhost:3000");
        assertThat(body.get("html").asString())
                .contains("Acessar o backoffice")
                .contains("href=\"http://localhost:3000\"");
    }

    @Test
    void sendEmail_badRequest_throwsEmailRefused() {
        server.on("POST", PATH, Response.of(400, "{\"success\":false,\"errors\":[\"to is invalid\",\"x\"]}"));

        assertThatThrownBy(() -> gateway.sendEmail(approvedInput()))
                .isInstanceOf(EmailRefusedException.class)
                .hasMessage("Email was refused by the provider");
    }

    @Test
    void sendEmail_unprocessable_throwsEmailRefused() {
        server.on("POST", PATH, Response.of(422, "{\"success\":false,\"errors\":[\"rejected\"]}"));

        assertThatThrownBy(() -> gateway.sendEmail(approvedInput()))
                .isInstanceOf(EmailRefusedException.class);
    }

    private void assertUnavailable() {
        assertThatThrownBy(() -> gateway.sendEmail(approvedInput()))
                .isInstanceOf(EmailProviderUnavailableException.class)
                .hasMessage("Email provider is unavailable");
    }

    @Test
    void sendEmail_unauthorized_throwsUnavailable() {
        server.on("POST", PATH, Response.of(401, "{\"success\":false,\"errors\":[\"Unauthorized\"]}"));

        assertUnavailable();
    }

    @Test
    void sendEmail_forbidden_throwsUnavailable() {
        server.on("POST", PATH, Response.of(403, "{\"success\":false,\"errors\":[\"Forbidden\"]}"));

        assertUnavailable();
    }

    @Test
    void sendEmail_notFound_throwsUnavailable() {
        server.on("POST", PATH, Response.of(404, "{\"success\":false,\"errors\":[\"Not found\"]}"));

        assertUnavailable();
    }

    @Test
    void sendEmail_tooManyRequests_throwsUnavailable() {
        server.on("POST", PATH, Response.of(429, "{\"success\":false,\"errors\":[\"Too many requests\"]}"));

        assertUnavailable();
    }

    @Test
    void sendEmail_serverErrorWithBody_throwsUnavailable() {
        server.on("POST", PATH, Response.of(500, "{\"success\":false,\"errors\":[\"Internal\",\"x\"]}"));

        assertUnavailable();
    }

    @Test
    void sendEmail_serverErrorWithoutBody_throwsUnavailable() {
        server.on("POST", PATH, Response.empty(500));

        assertUnavailable();
    }

    @Test
    void sendEmail_okWithEmptyBody_throwsUnavailable() {
        server.on("POST", PATH, Response.empty(200));

        assertUnavailable();
    }

    @Test
    void sendEmail_badRequestWithEmptyBody_throwsEmailRefused() {
        server.on("POST", PATH, Response.empty(400));

        assertThatThrownBy(() -> gateway.sendEmail(approvedInput()))
                .isInstanceOf(EmailRefusedException.class);
    }

    @Test
    void sendEmail_readTimeoutExceeded_throwsUnavailable() {
        server.on("POST", PATH, Response.of(200, "{\"success\":true,\"message_ids\":[\"id-1\"]}"));
        server.delay("POST", PATH, 1500);

        assertUnavailable();
    }

    @Test
    void sendEmail_serverDown_throwsUnavailable() {
        server.close();

        assertUnavailable();
    }

    @Test
    void sendEmail_unknownTemplate_throwsTemplateError() {
        var input = new SendEmailInputDTO("maria@empresa.com", "Maria", "s", "missing-template", Map.of());

        assertThatThrownBy(() -> gateway.sendEmail(input))
                .isInstanceOf(TemplateInputException.class);
        assertThat(server.received()).isEmpty();
    }
}
