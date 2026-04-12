package dev.nameless.poc.sns.controller;

import dev.nameless.poc.sns.dto.SubscriptionDto;
import dev.nameless.poc.sns.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sns/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping("/sqs")
    public ResponseEntity<Map<String, String>> subscribeSqs(
            @RequestParam String topicArn,
            @RequestParam String queueUrl,
            @RequestBody(required = false) SubscriptionDto config) {
        Map<String, Object> filterPolicy = config != null ? config.filterPolicy() : null;
        boolean rawDelivery = config != null && config.rawMessageDelivery();
        return ResponseEntity.ok(service.subscribeSqs(topicArn, queueUrl, filterPolicy, rawDelivery));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listSubscriptions(
            @RequestParam String topicArn) {
        return ResponseEntity.ok(service.listSubscriptions(topicArn));
    }

    @GetMapping("/attributes")
    public ResponseEntity<Map<String, String>> getSubscriptionAttributes(
            @RequestParam String subscriptionArn) {
        return ResponseEntity.ok(service.getSubscriptionAttributes(subscriptionArn));
    }

    @PutMapping("/filter-policy")
    public ResponseEntity<Void> setFilterPolicy(
            @RequestParam String subscriptionArn,
            @RequestBody Map<String, Object> filterPolicy) {
        service.setFilterPolicy(subscriptionArn, filterPolicy);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/attributes")
    public ResponseEntity<Void> setSubscriptionAttribute(
            @RequestParam String subscriptionArn,
            @RequestParam String attributeName,
            @RequestParam String attributeValue) {
        service.setSubscriptionAttribute(subscriptionArn, attributeName, attributeValue);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unsubscribe(@RequestParam String subscriptionArn) {
        service.unsubscribe(subscriptionArn);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/dlq")
    public ResponseEntity<Void> configureDeadLetterQueue(
            @RequestParam String subscriptionArn,
            @RequestParam String dlqArn) {
        service.configureDeadLetterQueue(subscriptionArn, dlqArn);
        return ResponseEntity.ok().build();
    }
}
