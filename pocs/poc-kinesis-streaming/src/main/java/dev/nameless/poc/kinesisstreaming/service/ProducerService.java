package dev.nameless.poc.kinesisstreaming.service;

import dev.nameless.poc.kinesisstreaming.dto.BatchPutDto;
import dev.nameless.poc.kinesisstreaming.dto.PutRecordDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResultEntry;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ProducerService {

    private final KinesisClient kinesisClient;

    public ProducerService(KinesisClient kinesisClient) {
        this.kinesisClient = kinesisClient;
    }

    public Map<String, String> putRecord(PutRecordDto dto) {
        PutRecordResponse response = kinesisClient.putRecord(PutRecordRequest.builder()
                .streamName(dto.streamName())
                .partitionKey(dto.partitionKey())
                .data(SdkBytes.fromString(dto.data(), StandardCharsets.UTF_8))
                .build());

        return Map.of(
                "shardId", response.shardId(),
                "sequenceNumber", response.sequenceNumber()
        );
    }

    public Map<String, Object> putRecords(BatchPutDto dto) {
        List<PutRecordsRequestEntry> entries = dto.records().stream()
                .map(record -> PutRecordsRequestEntry.builder()
                        .partitionKey(record.partitionKey())
                        .data(SdkBytes.fromString(record.data(), StandardCharsets.UTF_8))
                        .build())
                .toList();

        PutRecordsResponse response = kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(dto.streamName())
                .records(entries)
                .build());

        List<Map<String, String>> results = response.records().stream()
                .map(this::resultEntryToMap)
                .toList();

        return Map.of(
                "failedRecordCount", response.failedRecordCount(),
                "records", results
        );
    }

    private Map<String, String> resultEntryToMap(PutRecordsResultEntry entry) {
        if (entry.errorCode() != null && !entry.errorCode().isEmpty()) {
            return Map.of(
                    "errorCode", entry.errorCode(),
                    "errorMessage", entry.errorMessage()
            );
        }
        return Map.of(
                "shardId", entry.shardId(),
                "sequenceNumber", entry.sequenceNumber()
        );
    }
}
