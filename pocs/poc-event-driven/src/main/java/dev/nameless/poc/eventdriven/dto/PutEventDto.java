package dev.nameless.poc.eventdriven.dto;

import java.util.Map;

public record PutEventDto(
        String source,
        String detailType,
        Map<String, Object> detail
) {}
