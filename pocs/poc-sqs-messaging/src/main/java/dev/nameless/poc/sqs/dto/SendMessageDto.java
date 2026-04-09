package dev.nameless.poc.sqs.dto;

import java.util.Map;

public record SendMessageDto(
        String body,
        Map<String, String> attributes,
        Integer delaySeconds,
        String groupId,
        String deduplicationId
) {}
