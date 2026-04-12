package dev.nameless.poc.sesemail.service;

import dev.nameless.poc.sesemail.AbstractLocalStackTest;
import dev.nameless.poc.sesemail.dto.SendEmailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class EmailSenderServiceTest extends AbstractLocalStackTest {

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private IdentityService identityService;

    @Value("${aws.ses.from-email}")
    private String fromEmail;

    @BeforeEach
    void verifyFromIdentity() {
        // SES requer que o remetente esteja verificado
        identityService.verifyEmailIdentity(fromEmail);
    }

    @Test
    void shouldSendSimpleTextEmail() {
        SendEmailDto dto = new SendEmailDto(
                "recipient@example.com",
                "Test Subject",
                "Hello, this is a plain text email.",
                null);

        String messageId = emailSenderService.sendEmail(dto);
        assertThat(messageId).isNotBlank();
    }

    @Test
    void shouldSendHtmlEmail() {
        SendEmailDto dto = new SendEmailDto(
                "recipient@example.com",
                "HTML Test",
                "Fallback text",
                "<h1>Hello</h1><p>This is an HTML email.</p>");

        String messageId = emailSenderService.sendHtmlEmail(dto);
        assertThat(messageId).isNotBlank();
    }

    @Test
    void shouldSendRawEmailWithAttachment() {
        byte[] attachmentData = "Hello from attachment!".getBytes(StandardCharsets.UTF_8);

        String messageId = emailSenderService.sendRawEmail(
                "recipient@example.com",
                "Raw Email Test",
                "This email has an attachment.",
                "hello.txt",
                attachmentData,
                "text/plain");

        assertThat(messageId).isNotBlank();
    }

    @Test
    void shouldSendEmailWithOnlyHtmlBody() {
        SendEmailDto dto = new SendEmailDto(
                "recipient@example.com",
                "HTML Only",
                null,
                "<h1>Only HTML</h1>");

        String messageId = emailSenderService.sendHtmlEmail(dto);
        assertThat(messageId).isNotBlank();
    }
}
