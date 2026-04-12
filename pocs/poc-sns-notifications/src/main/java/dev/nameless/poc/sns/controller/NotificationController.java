package dev.nameless.poc.sns.controller;

import dev.nameless.poc.sns.dto.BatchPublishDto;
import dev.nameless.poc.sns.service.NotificationPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sns/notifications")
public class NotificationController {

    private final NotificationPublisherService service;

    public NotificationController(NotificationPublisherService service) {
        this.service = service;
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(
            @RequestParam String topicArn,
            @RequestBody String message,
            @RequestParam(required = false) String subject) {
        return ResponseEntity.ok(service.publish(topicArn, message, subject));
    }

    @PostMapping("/publish-with-attributes")
    public ResponseEntity<Map<String, String>> publishWithAttributes(
            @RequestParam String topicArn,
            @RequestParam String message,
            @RequestParam(required = false) String subject,
            @RequestBody Map<String, String> attributes) {
        return ResponseEntity.ok(service.publishWithAttributes(topicArn, message, subject, attributes));
    }

    @PostMapping("/publish-fifo")
    public ResponseEntity<Map<String, String>> publishFifo(
            @RequestParam String topicArn,
            @RequestBody String message,
            @RequestParam String groupId,
            @RequestParam(required = false) String deduplicationId) {
        return ResponseEntity.ok(service.publishFifo(topicArn, message, groupId, deduplicationId));
    }

    @PostMapping("/publish-batch")
    public ResponseEntity<Map<String, Object>> publishBatch(
            @RequestParam String topicArn,
            @RequestBody BatchPublishDto batch) {
        return ResponseEntity.ok(service.publishBatch(topicArn, batch.messages()));
    }
}
