package dev.nameless.poc.sqs.controller;

import dev.nameless.poc.sqs.dto.QueueAttributesDto;
import dev.nameless.poc.sqs.service.QueuePolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sqs/policies")
public class QueuePolicyController {

    private final QueuePolicyService service;

    public QueuePolicyController(QueuePolicyService service) {
        this.service = service;
    }

    @PutMapping("/policy")
    public ResponseEntity<Void> setQueuePolicy(
            @RequestParam String queueUrl,
            @RequestBody String policyJson) {
        service.setQueuePolicy(queueUrl, policyJson);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/policy")
    public ResponseEntity<Map<String, String>> getQueuePolicy(@RequestParam String queueUrl) {
        return ResponseEntity.ok(service.getQueuePolicy(queueUrl));
    }

    @DeleteMapping("/policy")
    public ResponseEntity<Void> removeQueuePolicy(@RequestParam String queueUrl) {
        service.removeQueuePolicy(queueUrl);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/attributes")
    public ResponseEntity<Void> setAdvancedAttributes(
            @RequestParam String queueUrl,
            @RequestBody QueueAttributesDto attributes) {
        service.setAdvancedAttributes(queueUrl, attributes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/attributes")
    public ResponseEntity<Map<String, String>> getAdvancedAttributes(@RequestParam String queueUrl) {
        return ResponseEntity.ok(service.getAdvancedAttributes(queueUrl));
    }
}
