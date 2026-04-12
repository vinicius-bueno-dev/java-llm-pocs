package dev.nameless.poc.lambda.dto;

public record EventSourceMappingDto(
        String functionName,
        String eventSourceArn,
        Integer batchSize,
        boolean enabled
) {}
