package dev.nameless.poc.kinesisstreaming.controller;

import dev.nameless.poc.kinesisstreaming.service.ConsumerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/kinesis/consumer")
public class ConsumerController {

    private final ConsumerService consumerService;

    public ConsumerController(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @GetMapping("/iterator/{streamName}/{shardId}")
    public ResponseEntity<Map<String, String>> getShardIterator(
            @PathVariable String streamName,
            @PathVariable String shardId,
            @RequestParam(defaultValue = "TRIM_HORIZON") String iteratorType) {
        String iterator = consumerService.getShardIterator(streamName, shardId, iteratorType);
        return ResponseEntity.ok(Map.of("shardIterator", iterator));
    }

    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getRecords(
            @RequestParam String shardIterator,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(consumerService.getRecords(shardIterator, limit));
    }

    @GetMapping("/consume/{streamName}/{shardId}")
    public ResponseEntity<Map<String, Object>> consumeFromBeginning(
            @PathVariable String streamName,
            @PathVariable String shardId) {
        return ResponseEntity.ok(consumerService.consumeFromBeginning(streamName, shardId));
    }
}
