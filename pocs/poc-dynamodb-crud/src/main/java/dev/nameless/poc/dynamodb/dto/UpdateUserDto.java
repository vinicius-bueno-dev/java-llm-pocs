package dev.nameless.poc.dynamodb.dto;

public record UpdateUserDto(
        String name,
        String email,
        Long expectedVersion
) {}
