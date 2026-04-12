package dev.nameless.poc.secretsmanager.dto;

import java.util.Map;

public record CreateSecretDto(
        String name,
        String secretValue,
        Map<String, String> tags
) {}
