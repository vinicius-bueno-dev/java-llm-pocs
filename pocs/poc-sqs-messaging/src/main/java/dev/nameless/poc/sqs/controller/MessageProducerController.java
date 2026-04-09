package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.dto.BatchMessageDto;
import dev.nameless.poc.sqs.service.MessageProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sqs/messages")
public class MessageProducerController {

    private final MessageProducerService service;

    public MessageProducerController(MessageProducerService service) {
        this.service = service;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(
            @RequestParam String queueUrl,
            @RequestBody String body) {
        return ResponseEntity.ok(service.sendMessage(queueUrl, body));
    }

    @PostMapping("/send-with-attributes")
    public ResponseEntity<Map<String, String>> sendMessageWithAttributes(
            @RequestParam String queueUrl,
            @RequestParam String body,
            @RequestBody Map<String, String> attributes) {
        return ResponseEntity.ok(service.sendMessageWithAttributes(queueUrl, body, attributes));
    }

    @PostMapping("/send-with-delay")
    public ResponseEntity<Map<String, String>> sendMessageWithDelay(
            @RequestParam String queueUrl,
            @RequestBody String body,
            @RequestParam(defaultValue = "5") int delaySeconds) {
        return ResponseEntity.ok(service.sendMessageWithDelay(queueUrl, body, delaySeconds));
    }

    @PostMapping("/send-fifo")
    public ResponseEntity<Map<String, String>> sendFifoMessage(
            @RequestParam String queueUrl,
            @RequestBody String body,
            @RequestParam String groupId,
            @RequestParam(required = false) String deduplicationId) {
        return ResponseEntity.ok(service.sendFifoMessage(queueUrl, body, groupId, deduplicationId));
    }

    @PostMapping("/send-batch")
    public ResponseEntity<Map<String, Object>> sendMessageBatch(
            @RequestParam String queueUrl,
            @RequestBody BatchMessageDto batch) {
        return ResponseEntity.ok(service.sendMessageBatch(queueUrl, batch.messages()));
    }

    @PostMapping("/send-fifo-batch")
    public ResponseEntity<Map<String, Object>> sendFifoBatch(
            @RequestParam String queueUrl,
            @RequestBody BatchMessageDto batch) {
        return ResponseEntity.ok(service.sendFifoBatch(queueUrl, batch.messages()));
    }
}
