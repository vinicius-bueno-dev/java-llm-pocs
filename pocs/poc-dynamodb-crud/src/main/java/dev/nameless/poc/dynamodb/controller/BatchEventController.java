package dev.nameless.poc.dynamodb.controller;

import dev.nameless.poc.dynamodb.dto.EventDto;
import dev.nameless.poc.dynamodb.dto.PageResult;
import dev.nameless.poc.dynamodb.service.BatchEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dynamodb/events")
public class BatchEventController {

    private final BatchEventService service;

    public BatchEventController(BatchEventService service) {
        this.service = service;
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchInsert(@RequestBody List<EventDto> events) {
        int written = service.batchInsert(events);
        return ResponseEntity.ok(Map.of("written", written));
    }

    @GetMapping("/scan")
    public ResponseEntity<PageResult<EventDto>> scan(
            @RequestParam(defaultValue = "25") int limit,
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(service.scan(limit, pageToken));
    }
}
