package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.service.QueueOperationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sqs/queues")
public class QueueOperationsController {

    private final QueueOperationsService service;

    public QueueOperationsController(QueueOperationsService service) {
        this.service = service;
    }

    @PostMapping("/standard")
    public ResponseEntity<Map<String, String>> createStandardQueue(
            @RequestParam String name,
            @RequestBody(required = false) Map<String, String> attributes) {
        return ResponseEntity.ok(Map.of("queueUrl", service.createStandardQueue(name, attributes)));
    }

    @PostMapping("/fifo")
    public ResponseEntity<Map<String, String>> createFifoQueue(
            @RequestParam String name,
            @RequestParam(defaultValue = "true") boolean contentBasedDedup) {
        return ResponseEntity.ok(Map.of("queueUrl", service.createFifoQueue(name, contentBasedDedup)));
    }

    @GetMapping
    public ResponseEntity<List<String>> listQueues(
            @RequestParam(required = false) String prefix) {
        return ResponseEntity.ok(service.listQueues(prefix));
    }

    @GetMapping("/url")
    public ResponseEntity<Map<String, String>> getQueueUrl(@RequestParam String name) {
        return ResponseEntity.ok(Map.of("queueUrl", service.getQueueUrl(name)));
    }

    @GetMapping("/attributes")
    public ResponseEntity<Map<String, String>> getQueueAttributes(@RequestParam String queueUrl) {
        return ResponseEntity.ok(service.getQueueAttributes(queueUrl));
    }

    @PutMapping("/attributes")
    public ResponseEntity<Void> setQueueAttributes(
            @RequestParam String queueUrl,
            @RequestBody Map<String, String> attributes) {
        service.setQueueAttributes(queueUrl, attributes);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteQueue(@RequestParam String queueUrl) {
        service.deleteQueue(queueUrl);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purge")
    public ResponseEntity<Void> purgeQueue(@RequestParam String queueUrl) {
        service.purgeQueue(queueUrl);
        return ResponseEntity.ok().build();
    }
}
