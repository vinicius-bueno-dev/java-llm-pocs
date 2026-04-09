package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.service.QueueTagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sqs/tags")
public class QueueTagController {

    private final QueueTagService service;

    public QueueTagController(QueueTagService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> tagQueue(
            @RequestParam String queueUrl,
            @RequestBody Map<String, String> tags) {
        service.tagQueue(queueUrl, tags);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> listQueueTags(@RequestParam String queueUrl) {
        return ResponseEntity.ok(service.listQueueTags(queueUrl));
    }

    @DeleteMapping
    public ResponseEntity<Void> untagQueue(
            @RequestParam String queueUrl,
            @RequestBody List<String> tagKeys) {
        service.untagQueue(queueUrl, tagKeys);
        return ResponseEntity.noContent().build();
    }
}
