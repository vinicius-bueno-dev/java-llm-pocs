package dev.nameless.poc.sesemail.service;

import dev.nameless.poc.sesemail.AbstractLocalStackTest;
import dev.nameless.poc.sesemail.dto.SendTemplatedEmailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.ses.model.Template;
import software.amazon.awssdk.services.ses.model.TemplateMetadata;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateServiceTest extends AbstractLocalStackTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private IdentityService identityService;

    @Value("${aws.ses.from-email}")
    private String fromEmail;

    @BeforeEach
    void verifyFromIdentity() {
        identityService.verifyEmailIdentity(fromEmail);
    }

    @Test
    void shouldCreateAndGetTemplate() {
        templateService.createTemplate(
                "welcome-template",
                "Welcome {{name}}!",
                "<h1>Hello {{name}}</h1>",
                "Hello {{name}}");

        Template template = templateService.getTemplate("welcome-template");

        assertThat(template.templateName()).isEqualTo("welcome-template");
        assertThat(template.subjectPart()).isEqualTo("Welcome {{name}}!");
        assertThat(template.htmlPart()).isEqualTo("<h1>Hello {{name}}</h1>");
        assertThat(template.textPart()).isEqualTo("Hello {{name}}");
    }

    @Test
    void shouldListTemplates() {
        templateService.createTemplate("tpl-one", "Subject 1", "<p>1</p>", "1");
        templateService.createTemplate("tpl-two", "Subject 2", "<p>2</p>", "2");

        List<TemplateMetadata> templates = templateService.listTemplates();

        assertThat(templates)
                .extracting(TemplateMetadata::name)
                .containsExactlyInAnyOrder("tpl-one", "tpl-two");
    }

    @Test
    void shouldDeleteTemplate() {
        templateService.createTemplate("to-delete", "Subject", "<p>x</p>", "x");
        assertThat(templateService.listTemplates()).hasSize(1);

        templateService.deleteTemplate("to-delete");

        assertThat(templateService.listTemplates()).isEmpty();
    }

    @Test
    void shouldSendTemplatedEmail() {
        templateService.createTemplate(
                "order-confirmation",
                "Order {{orderId}} confirmed",
                "<h1>Order {{orderId}}</h1><p>Thank you, {{name}}!</p>",
                "Order {{orderId}} - Thank you, {{name}}!");

        SendTemplatedEmailDto dto = new SendTemplatedEmailDto(
                "customer@example.com",
                "order-confirmation",
                Map.of("orderId", "12345", "name", "Alice"));

        String messageId = templateService.sendTemplatedEmail(dto);
        assertThat(messageId).isNotBlank();
    }
}
