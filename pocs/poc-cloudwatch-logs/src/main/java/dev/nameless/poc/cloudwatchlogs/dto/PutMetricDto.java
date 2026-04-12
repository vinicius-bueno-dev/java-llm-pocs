package dev.nameless.poc.cloudwatchlogs.dto;

public record PutMetricDto(String namespace, String metricName, Double value, String unit) {
}
