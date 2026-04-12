package dev.nameless.poc.kinesisstreaming.service;

import dev.nameless.poc.kinesisstreaming.AbstractLocalStackTest;
import dev.nameless.poc.kinesisstreaming.dto.BatchPutDto;
import dev.nameless.poc.kinesisstreaming.dto.PutRecordDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProducerServiceTest extends AbstractLocalStackTest {

    private static final String STREAM = "producer-test";

    @Autowired
    private ProducerService producerService;

    @BeforeEach
    void setUpStream() {
        createAndWaitForStream(STREAM, 1);
    }

    @Test
    void shouldPutSingleRecord() {
        PutRecordDto dto = new PutRecordDto(STREAM, "key-1", "{\"msg\":\"hello\"}");

        Map<String, String> result = producerService.putRecord(dto);

        assertThat(result).containsKey("shardId");
        assertThat(result).containsKey("sequenceNumber");
        assertThat(result.get("shardId")).isNotBlank();
        assertThat(result.get("sequenceNumber")).isNotBlank();
    }

    @Test
    void shouldPutBatchRecords() {
        List<PutRecordDto> records = List.of(
                new PutRecordDto(STREAM, "key-1", "{\"index\":1}"),
                new PutRecordDto(STREAM, "key-2", "{\"index\":2}"),
                new PutRecordDto(STREAM, "key-3", "{\"index\":3}")
        );
        BatchPutDto batchDto = new BatchPutDto(STREAM, records);

        Map<String, Object> result = producerService.putRecords(batchDto);

        assertThat(result.get("failedRecordCount")).isEqualTo(0);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> resultRecords = (List<Map<String, String>>) result.get("records");
        assertThat(resultRecords).hasSize(3);
        resultRecords.forEach(r -> {
            assertThat(r).containsKey("shardId");
            assertThat(r).containsKey("sequenceNumber");
        });
    }
}
