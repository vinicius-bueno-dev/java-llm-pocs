package dev.nameless.poc.kinesisstreaming.dto;

import java.util.List;

public record BatchPutDto(
        String streamName,
        List<PutRecordDto> records
) {
}
