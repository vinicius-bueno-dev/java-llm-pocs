package dev.nameless.poc.kinesisstreaming.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ConsumerService {

    private final KinesisClient kinesisClient;

    public ConsumerService(KinesisClient kinesisClient) {
        this.kinesisClient = kinesisClient;
    }

    public String getShardIterator(String streamName, String shardId, String iteratorType) {
        ShardIteratorType type = ShardIteratorType.fromValue(iteratorType);

        GetShardIteratorResponse response = kinesisClient.getShardIterator(
                GetShardIteratorRequest.builder()
                        .streamName(streamName)
                        .shardId(shardId)
                        .shardIteratorType(type)
                        .build());

        return response.shardIterator();
    }

    public Map<String, Object> getRecords(String shardIterator, int limit) {
        GetRecordsResponse response = kinesisClient.getRecords(
                GetRecordsRequest.builder()
                        .shardIterator(shardIterator)
                        .limit(limit)
                        .build());

        List<Map<String, String>> records = response.records().stream()
                .map(this::recordToMap)
                .toList();

        return Map.of(
                "records", records,
                "nextShardIterator", response.nextShardIterator() != null
                        ? response.nextShardIterator() : "",
                "millisBehindLatest", response.millisBehindLatest()
        );
    }

    public Map<String, Object> consumeFromBeginning(String streamName, String shardId) {
        String iterator = getShardIterator(streamName, shardId,
                ShardIteratorType.TRIM_HORIZON.toString());

        GetRecordsResponse response = kinesisClient.getRecords(
                GetRecordsRequest.builder()
                        .shardIterator(iterator)
                        .limit(100)
                        .build());

        List<Map<String, String>> records = response.records().stream()
                .map(this::recordToMap)
                .toList();

        return Map.of(
                "records", records,
                "nextShardIterator", response.nextShardIterator() != null
                        ? response.nextShardIterator() : "",
                "millisBehindLatest", response.millisBehindLatest()
        );
    }

    private Map<String, String> recordToMap(Record record) {
        return Map.of(
                "sequenceNumber", record.sequenceNumber(),
                "partitionKey", record.partitionKey(),
                "data", record.data().asString(StandardCharsets.UTF_8)
        );
    }
}
