package dev.nameless.poc.sesemail.dto;

public record SendEmailDto(
        String to,
        String subject,
        String bodyText,
        String bodyHtml
) {
}
