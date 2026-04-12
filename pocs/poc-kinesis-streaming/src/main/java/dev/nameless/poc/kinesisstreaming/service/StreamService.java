package dev.nameless.poc.kinesisstreaming.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DeleteStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsResponse;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;
import software.amazon.awssdk.services.kinesis.model.Shard;
import software.amazon.awssdk.services.kinesis.model.StreamDescription;

import java.util.List;
import java.util.Map;

@Service
public class StreamService {

    private final KinesisClient kinesisClient;

    public StreamService(KinesisClient kinesisClient) {
        this.kinesisClient = kinesisClient;
    }

    public void createStream(String streamName, int shardCount) {
        kinesisClient.createStream(CreateStreamRequest.builder()
                .streamName(streamName)
                .shardCount(shardCount)
                .build());
    }

    public List<String> listStreams() {
        ListStreamsResponse response = kinesisClient.listStreams(
                ListStreamsRequest.builder().build());
        return response.streamNames();
    }

    public Map<String, Object> describeStream(String streamName) {
        DescribeStreamResponse response = kinesisClient.describeStream(
                DescribeStreamRequest.builder()
                        .streamName(streamName)
                        .build());

        StreamDescription desc = response.streamDescription();
        return Map.of(
                "streamName", desc.streamName(),
                "streamARN", desc.streamARN(),
                "streamStatus", desc.streamStatusAsString(),
                "shardCount", desc.shards().size(),
                "retentionPeriodHours", desc.retentionPeriodHours()
        );
    }

    public void deleteStream(String streamName) {
        kinesisClient.deleteStream(DeleteStreamRequest.builder()
                .streamName(streamName)
                .enforceConsumerDeletion(true)
                .build());
    }

    public List<Map<String, String>> listShards(String streamName) {
        ListShardsResponse response = kinesisClient.listShards(
                ListShardsRequest.builder()
                        .streamName(streamName)
                        .build());

        return response.shards().stream()
                .map(this::shardToMap)
                .toList();
    }

    private Map<String, String> shardToMap(Shard shard) {
        return Map.of(
                "shardId", shard.shardId(),
                "hashKeyRangeStart", shard.hashKeyRange().startingHashKey(),
                "hashKeyRangeEnd", shard.hashKeyRange().endingHashKey(),
                "sequenceNumberRangeStart", shard.sequenceNumberRange().startingSequenceNumber()
        );
    }
}
