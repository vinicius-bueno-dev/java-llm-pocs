package dev.nameless.poc.sns.controller;

import dev.nameless.poc.sns.service.FanOutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sns/fanout")
public class FanOutController {

    private final FanOutService service;

    public FanOutController(FanOutService service) {
        this.service = service;
    }

    @PostMapping("/publish-and-verify")
    public ResponseEntity<Map<String, Object>> publishAndVerifyFanOut(
            @RequestParam String topicArn,
            @RequestBody String message,
            @RequestParam(required = false) Map<String, String> attributes,
            @RequestParam List<String> subscriberQueueUrls) {
        return ResponseEntity.ok(service.publishAndVerifyFanOut(
                topicArn, message, attributes, subscriberQueueUrls));
    }

    @PostMapping("/filtered")
    public ResponseEntity<Map<String, Object>> demonstrateFilteredFanOut(
            @RequestParam String topicArn,
            @RequestBody String message,
            @RequestParam String eventType,
            @RequestParam List<String> subscriberQueueUrls) {
        return ResponseEntity.ok(service.demonstrateFilteredFanOut(
                topicArn, message, eventType, subscriberQueueUrls));
    }
}
