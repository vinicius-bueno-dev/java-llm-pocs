package dev.nameless.poc.sns.controller;

import dev.nameless.poc.sns.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sns/topics")
public class TopicController {

    private final TopicService service;

    public TopicController(TopicService service) {
        this.service = service;
    }

    @PostMapping("/standard")
    public ResponseEntity<Map<String, String>> createTopic(
            @RequestParam String name,
            @RequestBody(required = false) Map<String, String> tags) {
        return ResponseEntity.ok(service.createTopic(name, tags));
    }

    @PostMapping("/fifo")
    public ResponseEntity<Map<String, String>> createFifoTopic(
            @RequestParam String name,
            @RequestParam(defaultValue = "true") boolean contentBasedDedup) {
        return ResponseEntity.ok(service.createFifoTopic(name, contentBasedDedup));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listTopics() {
        return ResponseEntity.ok(service.listTopics());
    }

    @GetMapping("/attributes")
    public ResponseEntity<Map<String, String>> getTopicAttributes(@RequestParam String topicArn) {
        return ResponseEntity.ok(service.getTopicAttributes(topicArn));
    }

    @PutMapping("/attributes")
    public ResponseEntity<Void> setTopicAttribute(
            @RequestParam String topicArn,
            @RequestParam String attributeName,
            @RequestParam String attributeValue) {
        service.setTopicAttribute(topicArn, attributeName, attributeValue);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTopic(@RequestParam String topicArn) {
        service.deleteTopic(topicArn);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tags")
    public ResponseEntity<Void> tagTopic(
            @RequestParam String topicArn,
            @RequestBody Map<String, String> tags) {
        service.tagTopic(topicArn, tags);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tags")
    public ResponseEntity<Map<String, String>> listTopicTags(@RequestParam String topicArn) {
        return ResponseEntity.ok(service.listTopicTags(topicArn));
    }
}
