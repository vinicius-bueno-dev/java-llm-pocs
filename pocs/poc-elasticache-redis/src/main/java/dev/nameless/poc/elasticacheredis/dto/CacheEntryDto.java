package dev.nameless.poc.elasticacheredis.dto;

public record CacheEntryDto(String key, String value, Long ttlSeconds) {
}
