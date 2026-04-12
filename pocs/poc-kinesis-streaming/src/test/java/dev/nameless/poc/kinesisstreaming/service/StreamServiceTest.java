package dev.nameless.poc.kinesisstreaming.service;

import dev.nameless.poc.kinesisstreaming.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.kinesis.model.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamServiceTest extends AbstractLocalStackTest {

    @Autowired
    private StreamService streamService;

    @Test
    void shouldCreateAndListStream() {
        streamService.createStream("test-stream", 1);
        kinesisClient.waiter().waitUntilStreamExists(b -> b.streamName("test-stream"));

        List<String> streams = streamService.listStreams();
        assertThat(streams).contains("test-stream");
    }

    @Test
    void shouldDescribeStream() {
        createAndWaitForStream("describe-test", 2);

        Map<String, Object> description = streamService.describeStream("describe-test");

        assertThat(description.get("streamName")).isEqualTo("describe-test");
        assertThat(description.get("streamStatus")).isEqualTo("ACTIVE");
        assertThat(description.get("shardCount")).isEqualTo(2);
    }

    @Test
    void shouldDeleteStream() {
        createAndWaitForStream("delete-test", 1);

        streamService.deleteStream("delete-test");

        assertThatThrownBy(() -> streamService.describeStream("delete-test"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldListShards() {
        createAndWaitForStream("shards-test", 2);

        List<Map<String, String>> shards = streamService.listShards("shards-test");

        assertThat(shards).hasSize(2);
        assertThat(shards.get(0)).containsKey("shardId");
        assertThat(shards.get(0)).containsKey("hashKeyRangeStart");
    }
}
