package dev.nameless.poc.sqs.dto;

public record QueueAttributesDto(
        Integer visibilityTimeout,
        Integer messageRetentionPeriod,
        Integer maximumMessageSize,
        Integer receiveMessageWaitTimeSeconds
) {}
