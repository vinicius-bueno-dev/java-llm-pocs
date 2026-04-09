package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.dto.RedriveConfigDto;
import dev.nameless.poc.sqs.service.DeadLetterQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sqs/dlq")
public class DeadLetterQueueController {

    private final DeadLetterQueueService service;

    public DeadLetterQueueController(DeadLetterQueueService service) {
        this.service = service;
    }

    @PutMapping("/redrive-policy")
    public ResponseEntity<Void> configureRedrivePolicy(
            @RequestParam String queueUrl,
            @RequestBody RedriveConfigDto config) {
        service.configureRedrivePolicy(queueUrl, config);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/redrive-policy")
    public ResponseEntity<Map<String, String>> getRedrivePolicy(@RequestParam String queueUrl) {
        return ResponseEntity.ok(service.getRedrivePolicy(queueUrl));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Map<String, String>>> listDlqMessages(
            @RequestParam String dlqUrl,
            @RequestParam(defaultValue = "10") int maxMessages) {
        return ResponseEntity.ok(service.listDlqMessages(dlqUrl, maxMessages));
    }

    @PostMapping("/redrive")
    public ResponseEntity<Map<String, String>> startMessageMoveTask(
            @RequestParam String dlqArn,
            @RequestParam String destinationQueueArn) {
        return ResponseEntity.ok(service.startMessageMoveTask(dlqArn, destinationQueueArn));
    }

    @PostMapping("/simulate-failure")
    public ResponseEntity<Map<String, Object>> simulateFailure(
            @RequestParam String queueUrl,
            @RequestParam(defaultValue = "4") int receiveCount) {
        return ResponseEntity.ok(service.simulateFailure(queueUrl, receiveCount));
    }
}
