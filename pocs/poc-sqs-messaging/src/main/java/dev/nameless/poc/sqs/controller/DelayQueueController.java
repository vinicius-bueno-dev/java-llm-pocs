package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.service.DelayQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sqs/delay")
public class DelayQueueController {

    private final DelayQueueService service;

    public DelayQueueController(DelayQueueService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createDelayQueue(
            @RequestParam String name,
            @RequestParam(defaultValue = "10") int delaySeconds) {
        return ResponseEntity.ok(Map.of("queueUrl", service.createDelayQueue(name, delaySeconds)));
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendWithMessageDelay(
            @RequestParam String queueUrl,
            @RequestBody String body,
            @RequestParam(defaultValue = "5") int delaySeconds) {
        return ResponseEntity.ok(service.sendWithMessageDelay(queueUrl, body, delaySeconds));
    }

    @PostMapping("/demonstrate")
    public ResponseEntity<Map<String, Object>> demonstrateDelay(
            @RequestParam String queueUrl,
            @RequestBody String body,
            @RequestParam(defaultValue = "5") int delaySeconds) {
        return ResponseEntity.ok(service.demonstrateDelay(queueUrl, body, delaySeconds));
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getDelayConfig(@RequestParam String queueUrl) {
        return ResponseEntity.ok(service.getDelayConfig(queueUrl));
    }
}
