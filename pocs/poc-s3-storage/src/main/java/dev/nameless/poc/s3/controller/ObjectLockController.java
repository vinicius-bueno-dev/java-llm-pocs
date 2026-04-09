package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.ObjectLockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/object-lock")
public class ObjectLockController {

    private final ObjectLockService service;

    public ObjectLockController(ObjectLockService service) {
        this.service = service;
    }

    @PostMapping("/buckets")
    public ResponseEntity<Map<String, String>> createBucketWithObjectLock(@RequestParam String name) {
        return ResponseEntity.ok(Map.of("bucket", service.createBucketWithObjectLock(name)));
    }

    @PutMapping("/{bucket}/retention")
    public ResponseEntity<Void> putDefaultRetention(
            @PathVariable String bucket,
            @RequestParam(defaultValue = "GOVERNANCE") String mode,
            @RequestParam(defaultValue = "1") int days) {
        service.putDefaultRetention(bucket, mode, days);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucket}/config")
    public ResponseEntity<Map<String, String>> getObjectLockConfiguration(@PathVariable String bucket) {
        return ResponseEntity.ok(service.getObjectLockConfiguration(bucket));
    }

    @PutMapping("/{bucket}/objects/{key}/retention")
    public ResponseEntity<Void> putObjectRetention(
            @PathVariable String bucket,
            @PathVariable String key,
            @RequestParam(defaultValue = "GOVERNANCE") String mode,
            @RequestParam(defaultValue = "1") int days) {
        Instant retainUntil = Instant.now().plus(days, ChronoUnit.DAYS);
        service.putObjectRetention(bucket, key, mode, retainUntil);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucket}/objects/{key}/retention")
    public ResponseEntity<Map<String, String>> getObjectRetention(
            @PathVariable String bucket,
            @PathVariable String key) {
        return ResponseEntity.ok(service.getObjectRetention(bucket, key));
    }

    @PutMapping("/{bucket}/objects/{key}/legal-hold")
    public ResponseEntity<Void> putLegalHold(
            @PathVariable String bucket,
            @PathVariable String key,
            @RequestParam boolean enabled) {
        service.putLegalHold(bucket, key, enabled);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucket}/objects/{key}/legal-hold")
    public ResponseEntity<Map<String, String>> getLegalHold(
            @PathVariable String bucket,
            @PathVariable String key) {
        return ResponseEntity.ok(service.getLegalHold(bucket, key));
    }
}
