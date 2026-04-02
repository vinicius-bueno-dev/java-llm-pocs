package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PutMapping("/sqs")
    public ResponseEntity<Void> configureQueueNotification(
            @RequestParam String bucket, @RequestParam String queueArn,
            @RequestParam List<String> events,
            @RequestParam(defaultValue = "") String filterPrefix) {
        service.configureQueueNotification(bucket, queueArn, events, filterPrefix);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sns")
    public ResponseEntity<Void> configureTopicNotification(
            @RequestParam String bucket, @RequestParam String topicArn,
            @RequestParam List<String> events,
            @RequestParam(defaultValue = "") String filterPrefix) {
        service.configureTopicNotification(bucket, topicArn, events, filterPrefix);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getNotificationConfiguration(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getNotificationConfiguration(bucket));
    }

    @DeleteMapping("/config")
    public ResponseEntity<Void> deleteNotificationConfiguration(@RequestParam String bucket) {
        service.deleteNotificationConfiguration(bucket);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/poll-sqs")
    public ResponseEntity<List<String>> pollSqsMessages(
            @RequestParam String queueUrl,
            @RequestParam(defaultValue = "10") int maxMessages) {
        return ResponseEntity.ok(service.pollSqsMessages(queueUrl, maxMessages));
    }
}
