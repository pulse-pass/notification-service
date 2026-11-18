package com.pulsepass.notificationservice.unit.infrastructure.gateways.mailtrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.notificationservice.infrastructure.gateways.mailtrap.EmailTemplateConfig;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;

class EmailTemplateConfigTest {

    private final EmailTemplateConfig config = new EmailTemplateConfig();

    private Context context(Map<String, Object> variables) {
        var context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariables(variables);
        return context;
    }

    @Test
    void emailTemplateEngine_approvedHtml_rendersNameCompanyAndLink() {
        var html = config.emailTemplateEngine().process("email/organizer-approved.html", context(Map.of(
                "recipient_name", "Maria", "legal_name", "Eventos Silva LTDA")));

        assertThat(html).contains("Olá, <span>Maria</span>")
                .contains("<strong>Eventos Silva LTDA</strong> foi aprovado")
                .contains("href=\"http://localhost:3000\"")
                .contains("Acessar o backoffice")
                .contains("#E5E5E5")
                .contains("#FF6B5B");
    }

    @Test
    void emailTemplateEngine_approvedText_rendersPlainText() {
        var text = config.emailTemplateEngine().process("email/organizer-approved.txt", context(Map.of(
                "recipient_name", "Maria", "legal_name", "Eventos Silva LTDA")));

        assertThat(text).contains("Olá, Maria")
                .contains("O cadastro da Eventos Silva LTDA foi aprovado")
                .contains("Acessar o backoffice: http://localhost:3000");
    }

    @Test
    void emailTemplateEngine_rejectedHtml_rendersReason() {
        var html = config.emailTemplateEngine().process("email/organizer-rejected.html", context(Map.of(
                "recipient_name", "Maria", "legal_name", "Eventos Silva LTDA",
                "rejection_reason_description", "CNPJ inapto")));

        assertThat(html).contains("foi reprovado").contains("<strong>Motivo:</strong> <span>CNPJ inapto</span>");
    }

    @Test
    void emailTemplateEngine_rejectedText_rendersReason() {
        var text = config.emailTemplateEngine().process("email/organizer-rejected.txt", context(Map.of(
                "recipient_name", "Maria", "legal_name", "Eventos Silva LTDA",
                "rejection_reason_description", "CNPJ inapto")));

        assertThat(text).contains("foi reprovado").contains("Motivo: CNPJ inapto");
    }

    @Test
    void emailTemplateEngine_htmlInVariable_isEscaped() {
        var html = config.emailTemplateEngine().process("email/organizer-rejected.html", context(Map.of(
                "recipient_name", "<b>Maria</b>", "legal_name", "<script>x</script>",
                "rejection_reason_description", "<i>motivo</i>")));

        assertThat(html).contains("&lt;b&gt;Maria&lt;/b&gt;")
                .contains("&lt;script&gt;x&lt;/script&gt;")
                .doesNotContain("<script>");
    }

    @Test
    void emailTemplateEngine_textWithSpecialCharacters_keepsThemUnescaped() {
        var text = config.emailTemplateEngine().process("email/organizer-rejected.txt", context(Map.of(
                "recipient_name", "Maria & <Co>", "legal_name", "Silva & Filhos",
                "rejection_reason_description", "CNPJ inapto")));

        assertThat(text).contains("Olá, Maria & <Co>").contains("Silva & Filhos").doesNotContain("&amp;");
    }
}
