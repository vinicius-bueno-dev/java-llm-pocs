package dev.nameless.poc.elasticacheredis.controller;

import dev.nameless.poc.elasticacheredis.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/redis/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createSession(@RequestBody Map<String, String> attributes) {
        String sessionId = sessionService.createSession(attributes);
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<Map<Object, Object>> getSession(@PathVariable String sessionId) {
        Map<Object, Object> session = sessionService.getSession(sessionId);
        if (session.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        Boolean deleted = sessionService.deleteSession(sessionId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "deleted", deleted));
    }

    @PatchMapping("/{sessionId}/extend")
    public ResponseEntity<Map<String, Object>> extendSession(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1800") long extraSeconds) {
        Boolean extended = sessionService.extendSession(sessionId, extraSeconds);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "extended", extended));
    }
}
