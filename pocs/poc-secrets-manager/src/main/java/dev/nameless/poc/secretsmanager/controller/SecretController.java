package dev.nameless.poc.secretsmanager.controller;

import dev.nameless.poc.secretsmanager.dto.CreateSecretDto;
import dev.nameless.poc.secretsmanager.dto.UpdateSecretDto;
import dev.nameless.poc.secretsmanager.service.SecretService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/secrets")
public class SecretController {

    private final SecretService service;

    public SecretController(SecretService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createSecret(@RequestBody CreateSecretDto dto) {
        return ResponseEntity.ok(service.createSecret(dto.name(), dto.secretValue(), dto.tags()));
    }

    @GetMapping("/{secretId}")
    public ResponseEntity<Map<String, Object>> getSecretValue(@PathVariable String secretId) {
        return ResponseEntity.ok(service.getSecretValue(secretId));
    }

    @GetMapping("/{secretId}/stage")
    public ResponseEntity<Map<String, Object>> getSecretValueByStage(
            @PathVariable String secretId,
            @RequestParam String versionStage) {
        return ResponseEntity.ok(service.getSecretValueByStage(secretId, versionStage));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listSecrets() {
        return ResponseEntity.ok(service.listSecrets());
    }

    @GetMapping("/{secretId}/describe")
    public ResponseEntity<Map<String, Object>> describeSecret(@PathVariable String secretId) {
        return ResponseEntity.ok(service.describeSecret(secretId));
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateSecretValue(@RequestBody UpdateSecretDto dto) {
        return ResponseEntity.ok(service.updateSecretValue(dto.secretId(), dto.secretValue()));
    }

    @PutMapping("/{secretId}/value")
    public ResponseEntity<Map<String, Object>> putSecretValue(
            @PathVariable String secretId,
            @RequestParam String value,
            @RequestParam(required = false) List<String> versionStages) {
        return ResponseEntity.ok(service.putSecretValue(secretId, value, versionStages));
    }

    @DeleteMapping("/{secretId}")
    public ResponseEntity<Void> deleteSecret(
            @PathVariable String secretId,
            @RequestParam(defaultValue = "false") boolean forceDelete) {
        service.deleteSecret(secretId, forceDelete);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{secretId}/restore")
    public ResponseEntity<Map<String, String>> restoreSecret(@PathVariable String secretId) {
        return ResponseEntity.ok(service.restoreSecret(secretId));
    }

    @PostMapping("/{secretId}/tags")
    public ResponseEntity<Void> tagSecret(
            @PathVariable String secretId,
            @RequestBody Map<String, String> tags) {
        service.tagSecret(secretId, tags);
        return ResponseEntity.ok().build();
    }
}
