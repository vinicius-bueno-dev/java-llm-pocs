package dev.nameless.poc.secretsmanager.controller;

import dev.nameless.poc.secretsmanager.service.SecretRotationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/secrets/rotation")
public class SecretRotationController {

    private final SecretRotationService service;

    public SecretRotationController(SecretRotationService service) {
        this.service = service;
    }

    @PostMapping("/{secretId}")
    public ResponseEntity<Map<String, String>> rotateSecret(@PathVariable String secretId) {
        return ResponseEntity.ok(service.rotateSecret(secretId));
    }

    @GetMapping("/{secretId}/config")
    public ResponseEntity<Map<String, Object>> getRotationConfig(@PathVariable String secretId) {
        return ResponseEntity.ok(service.getRotationConfig(secretId));
    }

    @GetMapping("/{secretId}/versions")
    public ResponseEntity<List<Map<String, Object>>> listSecretVersions(@PathVariable String secretId) {
        return ResponseEntity.ok(service.listSecretVersions(secretId));
    }
}
