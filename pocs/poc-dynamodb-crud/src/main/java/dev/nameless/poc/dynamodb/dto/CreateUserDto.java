package dev.nameless.poc.dynamodb.dto;

public record CreateUserDto(
        String tenantId,
        String userId,
        String email,
        String name,
        Long expiresAt
) {}
