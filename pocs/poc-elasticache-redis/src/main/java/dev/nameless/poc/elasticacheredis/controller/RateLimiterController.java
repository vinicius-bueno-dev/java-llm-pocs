package dev.nameless.poc.elasticacheredis.controller;

import dev.nameless.poc.elasticacheredis.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/redis/ratelimit")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<Map<String, Object>> checkRateLimit(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "10") int maxRequests,
            @RequestParam(defaultValue = "60") int windowSeconds) {

        boolean allowed = rateLimiterService.isAllowed(clientId, maxRequests, windowSeconds);

        Map<String, Object> body = Map.of(
                "clientId", clientId,
                "allowed", allowed,
                "maxRequests", maxRequests,
                "windowSeconds", windowSeconds
        );

        if (allowed) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}
