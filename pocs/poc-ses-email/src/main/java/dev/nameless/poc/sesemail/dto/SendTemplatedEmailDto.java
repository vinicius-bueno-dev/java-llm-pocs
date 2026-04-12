package dev.nameless.poc.sesemail.dto;

import java.util.Map;

public record SendTemplatedEmailDto(
        String to,
        String templateName,
        Map<String, String> templateData
) {
}
