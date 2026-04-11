package dev.nameless.poc.dynamodb.dto;

public record EventDto(
        String eventId,
        String type,
        String payload
) {}
