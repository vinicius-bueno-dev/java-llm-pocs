package dev.nameless.poc.sns.dto;

import java.util.Map;

public record SubscriptionDto(
        String protocol,
        String endpoint,
        Map<String, Object> filterPolicy,
        boolean rawMessageDelivery
) {}
