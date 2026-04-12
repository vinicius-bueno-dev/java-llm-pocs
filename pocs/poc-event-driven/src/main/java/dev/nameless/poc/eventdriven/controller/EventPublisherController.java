package dev.nameless.poc.eventdriven.controller;

import dev.nameless.poc.eventdriven.dto.PutEventDto;
import dev.nameless.poc.eventdriven.service.EventPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/publish")
public class EventPublisherController {

    private final EventPublisherService service;

    public EventPublisherController(EventPublisherService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> putEvent(
            @RequestParam String busName,
            @RequestBody PutEventDto dto) {
        return ResponseEntity.ok(service.putEvent(busName, dto));
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> putEventBatch(
            @RequestParam String busName,
            @RequestBody List<PutEventDto> events) {
        return ResponseEntity.ok(service.putEventBatch(busName, events));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> putEventAndVerify(
            @RequestParam String busName,
            @RequestParam List<String> targetQueueUrls,
            @RequestBody PutEventDto dto) {
        return ResponseEntity.ok(service.putEventAndVerifyDelivery(busName, dto, targetQueueUrls));
    }
}
