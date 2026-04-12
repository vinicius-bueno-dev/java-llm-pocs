package dev.nameless.poc.secretsmanager.dto;

public record UpdateSecretDto(
        String secretId,
        String secretValue
) {}
