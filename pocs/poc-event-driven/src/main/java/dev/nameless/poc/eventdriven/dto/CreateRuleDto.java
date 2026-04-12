package dev.nameless.poc.eventdriven.dto;

public record CreateRuleDto(
        String ruleName,
        String description,
        String eventPattern,
        String scheduleExpression,
        boolean enabled
) {}
