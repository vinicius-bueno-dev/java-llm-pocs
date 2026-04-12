package dev.nameless.poc.eventdriven.controller;

import dev.nameless.poc.eventdriven.service.EventBusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/buses")
public class EventBusController {

    private final EventBusService service;

    public EventBusController(EventBusService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createEventBus(@RequestParam String busName) {
        return ResponseEntity.ok(service.createEventBus(busName));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listEventBuses() {
        return ResponseEntity.ok(service.listEventBuses());
    }

    @GetMapping("/{busName}")
    public ResponseEntity<Map<String, String>> describeEventBus(@PathVariable String busName) {
        return ResponseEntity.ok(service.describeEventBus(busName));
    }

    @DeleteMapping("/{busName}")
    public ResponseEntity<Void> deleteEventBus(@PathVariable String busName) {
        service.deleteEventBus(busName);
        return ResponseEntity.noContent().build();
    }
}
