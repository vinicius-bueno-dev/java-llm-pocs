package dev.nameless.poc.lambda.dto;

import java.util.Map;

public record CreateFunctionDto(
        String functionName,
        String handler,
        String description,
        Integer timeout,
        Integer memorySize,
        Map<String, String> environmentVariables
) {}
