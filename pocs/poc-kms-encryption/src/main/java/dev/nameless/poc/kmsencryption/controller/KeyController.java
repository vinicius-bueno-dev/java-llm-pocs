package dev.nameless.poc.kmsencryption.controller;

import dev.nameless.poc.kmsencryption.service.KeyManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kms/keys")
public class KeyController {

    private final KeyManagementService keyManagementService;

    public KeyController(KeyManagementService keyManagementService) {
        this.keyManagementService = keyManagementService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createKey(@RequestBody Map<String, Object> body) {
        String description = (String) body.getOrDefault("description", "");
        @SuppressWarnings("unchecked")
        Map<String, String> tags = (Map<String, String>) body.get("tags");
        return ResponseEntity.ok(keyManagementService.createKey(description, tags));
    }

    @PostMapping("/with-alias")
    public ResponseEntity<Map<String, String>> createKeyWithAlias(@RequestBody Map<String, String> body) {
        String description = body.getOrDefault("description", "");
        String alias = body.get("alias");
        return ResponseEntity.ok(keyManagementService.createKeyWithAlias(description, alias));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listKeys() {
        return ResponseEntity.ok(keyManagementService.listKeys());
    }

    @GetMapping("/{keyId}")
    public ResponseEntity<Map<String, String>> describeKey(@PathVariable String keyId) {
        return ResponseEntity.ok(keyManagementService.describeKey(keyId));
    }

    @PutMapping("/{keyId}/rotation")
    public ResponseEntity<Void> enableKeyRotation(@PathVariable String keyId) {
        keyManagementService.enableKeyRotation(keyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{keyId}/rotation")
    public ResponseEntity<Map<String, Object>> getKeyRotationStatus(@PathVariable String keyId) {
        return ResponseEntity.ok(keyManagementService.getKeyRotationStatus(keyId));
    }

    @PostMapping("/aliases")
    public ResponseEntity<Void> createAlias(@RequestBody Map<String, String> body) {
        keyManagementService.createAlias(body.get("aliasName"), body.get("keyId"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/aliases")
    public ResponseEntity<List<Map<String, String>>> listAliases() {
        return ResponseEntity.ok(keyManagementService.listAliases());
    }

    @PutMapping("/{keyId}/disable")
    public ResponseEntity<Void> disableKey(@PathVariable String keyId) {
        keyManagementService.disableKey(keyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Map<String, String>> scheduleKeyDeletion(
            @PathVariable String keyId,
            @RequestParam(defaultValue = "7") int pendingWindowDays) {
        return ResponseEntity.ok(keyManagementService.scheduleKeyDeletion(keyId, pendingWindowDays));
    }
}
