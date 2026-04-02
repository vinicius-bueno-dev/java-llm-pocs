package dev.nameless.poc.s3.dto;

public record LifecycleRuleDto(
        String id,
        String prefix,
        int expirationDays,
        String transitionStorageClass,
        int transitionDays
) {}
