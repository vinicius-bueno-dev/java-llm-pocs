package dev.nameless.poc.lambda.controller;

import dev.nameless.poc.lambda.dto.EventSourceMappingDto;
import dev.nameless.poc.lambda.service.EventSourceMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lambda/event-sources")
public class EventSourceController {

    private final EventSourceMappingService eventSourceService;

    public EventSourceController(EventSourceMappingService eventSourceService) {
        this.eventSourceService = eventSourceService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createEventSourceMapping(
            @RequestBody EventSourceMappingDto dto) {
        return ResponseEntity.ok(eventSourceService.createEventSourceMapping(dto));
    }

    @GetMapping("/{functionName}")
    public ResponseEntity<List<Map<String, String>>> listEventSourceMappings(
            @PathVariable String functionName) {
        return ResponseEntity.ok(eventSourceService.listEventSourceMappings(functionName));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<Map<String, String>> updateEventSourceMapping(
            @PathVariable String uuid,
            @RequestParam(required = false) Integer batchSize,
            @RequestParam(defaultValue = "true") boolean enabled) {
        return ResponseEntity.ok(eventSourceService.updateEventSourceMapping(uuid, batchSize, enabled));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteEventSourceMapping(@PathVariable String uuid) {
        eventSourceService.deleteEventSourceMapping(uuid);
        return ResponseEntity.noContent().build();
    }
}
