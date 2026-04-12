package dev.nameless.poc.lambda.dto;

public record InvokeFunctionDto(
        String functionName,
        String payload,
        boolean async
) {}
