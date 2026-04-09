package dev.nameless.poc.sqs.dto;

public record RedriveConfigDto(
        String deadLetterQueueArn,
        int maxReceiveCount
) {}
