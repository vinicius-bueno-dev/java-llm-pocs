package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.service.MessageConsumerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sqs/consumer")
public class MessageConsumerController {

    private final MessageConsumerService service;

    public MessageConsumerController(MessageConsumerService service) {
        this.service = service;
    }

    @GetMapping("/receive")
    public ResponseEntity<List<Map<String, String>>> receiveMessages(
            @RequestParam String queueUrl,
            @RequestParam(defaultValue = "1") int maxMessages,
            @RequestParam(defaultValue = "0") int waitTimeSeconds) {
        return ResponseEntity.ok(service.receiveMessages(queueUrl, maxMessages, waitTimeSeconds));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteMessage(
            @RequestParam String queueUrl,
            @RequestParam String receiptHandle) {
        service.deleteMessage(queueUrl, receiptHandle);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-batch")
    public ResponseEntity<Map<String, Object>> deleteMessageBatch(
            @RequestParam String queueUrl,
            @RequestBody List<String> receiptHandles) {
        return ResponseEntity.ok(service.deleteMessageBatch(queueUrl, receiptHandles));
    }

    @PutMapping("/visibility")
    public ResponseEntity<Void> changeVisibilityTimeout(
            @RequestParam String queueUrl,
            @RequestParam String receiptHandle,
            @RequestParam int visibilityTimeout) {
        service.changeVisibilityTimeout(queueUrl, receiptHandle, visibilityTimeout);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/visibility-batch")
    public ResponseEntity<Map<String, Object>> changeVisibilityTimeoutBatch(
            @RequestParam String queueUrl,
            @RequestBody Map<String, Integer> receiptHandleToTimeout) {
        return ResponseEntity.ok(service.changeVisibilityTimeoutBatch(queueUrl, receiptHandleToTimeout));
    }
}
