package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.service.FifoQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sqs/fifo")
public class FifoQueueController {

    private final FifoQueueService service;

    public FifoQueueController(FifoQueueService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createFifoQueue(
            @RequestParam String name,
            @RequestParam(defaultValue = "true") boolean contentBasedDedup) {
        return ResponseEntity.ok(Map.of("queueUrl", service.createFifoQueue(name, contentBasedDedup)));
    }

    @PostMapping("/send-ordered")
    public ResponseEntity<List<Map<String, String>>> sendOrderedMessages(
            @RequestParam String queueUrl,
            @RequestParam String groupId,
            @RequestBody List<String> messages) {
        return ResponseEntity.ok(service.sendOrderedMessages(queueUrl, groupId, messages));
    }

    @PostMapping("/demonstrate-dedup")
    public ResponseEntity<Map<String, Object>> demonstrateDeduplication(
            @RequestParam String queueUrl,
            @RequestParam String groupId,
            @RequestBody String body) {
        return ResponseEntity.ok(service.demonstrateDeduplication(queueUrl, groupId, body));
    }

    @GetMapping("/receive-ordered")
    public ResponseEntity<List<Map<String, String>>> receiveOrdered(
            @RequestParam String queueUrl,
            @RequestParam(defaultValue = "10") int maxMessages) {
        return ResponseEntity.ok(service.receiveOrdered(queueUrl, maxMessages));
    }

    @PostMapping("/send-multi-group")
    public ResponseEntity<Map<String, Object>> sendToMultipleGroups(
            @RequestParam String queueUrl,
            @RequestBody Map<String, List<String>> groupMessages) {
        return ResponseEntity.ok(service.sendToMultipleGroups(queueUrl, groupMessages));
    }
}
