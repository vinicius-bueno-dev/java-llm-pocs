package dev.nameless.poc.kinesisstreaming.dto;

public record PutRecordDto(
        String streamName,
        String partitionKey,
        String data
) {
}
