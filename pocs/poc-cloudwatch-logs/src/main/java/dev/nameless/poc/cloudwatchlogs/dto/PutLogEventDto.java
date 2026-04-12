package dev.nameless.poc.cloudwatchlogs.dto;

public record PutLogEventDto(String message, Long timestamp) {

    public long effectiveTimestamp() {
        return timestamp != null ? timestamp : System.currentTimeMillis();
    }
}
