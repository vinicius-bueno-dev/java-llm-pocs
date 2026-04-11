package dev.nameless.poc.dynamodb.controller;

import dev.nameless.poc.dynamodb.service.TtlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dynamodb/ttl")
public class TtlController {

    private final TtlService service;

    public TtlController(TtlService service) {
        this.service = service;
    }

    @GetMapping("/describe")
    public ResponseEntity<Map<String, String>> describe() {
        return ResponseEntity.ok(Map.of("status", service.describeTtl()));
    }

    @PostMapping("/{tenantId}/{userId}")
    public ResponseEntity<Map<String, Object>> setExpiry(
            @PathVariable String tenantId,
            @PathVariable String userId,
            @RequestParam long seconds) {
        long expiresAt = service.setExpiresInSeconds(tenantId, userId, seconds);
        return ResponseEntity.ok(Map.of("expiresAt", expiresAt));
    }
}
