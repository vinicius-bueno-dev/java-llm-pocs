package dev.nameless.poc.s3.dto;

import java.util.List;

public record CorsRuleDto(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposeHeaders,
        int maxAgeSeconds
) {}
