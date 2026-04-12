package dev.nameless.poc.apigateway.dto;

public record CreateResourceDto(String restApiId, String parentId, String pathPart) {
}
