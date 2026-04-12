package dev.nameless.poc.kinesisstreaming.service;

import dev.nameless.poc.kinesisstreaming.AbstractLocalStackTest;
import dev.nameless.poc.kinesisstreaming.dto.PutRecordDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumerServiceTest extends AbstractLocalStackTest {

    private static final String STREAM = "consumer-test";

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private ProducerService producerService;

    @BeforeEach
    void setUpStream() {
        createAndWaitForStream(STREAM, 1);
    }

    @Test
    void shouldGetShardIterator() {
        String iterator = consumerService.getShardIterator(STREAM, "shardId-000000000000", "TRIM_HORIZON");

        assertThat(iterator).isNotBlank();
    }

    @Test
    void shouldGetRecordsWithIterator() {
        producerService.putRecord(new PutRecordDto(STREAM, "key-1", "record-data-1"));
        producerService.putRecord(new PutRecordDto(STREAM, "key-2", "record-data-2"));

        String iterator = consumerService.getShardIterator(STREAM, "shardId-000000000000", "TRIM_HORIZON");

        Map<String, Object> result = consumerService.getRecords(iterator, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> records = (List<Map<String, String>>) result.get("records");
        assertThat(records).hasSize(2);
        assertThat(records.get(0).get("data")).isEqualTo("record-data-1");
        assertThat(records.get(1).get("data")).isEqualTo("record-data-2");
    }

    @Test
    void shouldConsumeFromBeginning() {
        producerService.putRecord(new PutRecordDto(STREAM, "pk-1", "beginning-data"));

        Map<String, Object> result = consumerService.consumeFromBeginning(STREAM, "shardId-000000000000");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> records = (List<Map<String, String>>) result.get("records");
        assertThat(records).isNotEmpty();
        assertThat(records.get(0).get("data")).isEqualTo("beginning-data");
        assertThat(records.get(0)).containsKey("sequenceNumber");
        assertThat(records.get(0)).containsKey("partitionKey");
    }

    @Test
    void shouldReturnEmptyWhenNoRecords() {
        String iterator = consumerService.getShardIterator(STREAM, "shardId-000000000000", "TRIM_HORIZON");

        Map<String, Object> result = consumerService.getRecords(iterator, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> records = (List<Map<String, String>>) result.get("records");
        assertThat(records).isEmpty();
    }
}
