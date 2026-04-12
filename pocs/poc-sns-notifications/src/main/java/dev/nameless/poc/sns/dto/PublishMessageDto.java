package dev.nameless.poc.sns.dto;

import java.util.Map;

public record PublishMessageDto(
        String message,
        String subject,
        Map<String, String> attributes,
        String groupId,
        String deduplicationId
) {}
