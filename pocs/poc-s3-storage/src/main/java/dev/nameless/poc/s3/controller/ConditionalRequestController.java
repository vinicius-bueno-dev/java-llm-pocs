package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.ConditionalRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/conditional")
public class ConditionalRequestController {

    private final ConditionalRequestService service;

    public ConditionalRequestController(ConditionalRequestService service) {
        this.service = service;
    }

    @GetMapping("/if-match")
    public ResponseEntity<Map<String, Object>> getIfMatch(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam String etag) {
        return ResponseEntity.ok(service.getIfMatch(bucket, key, etag));
    }

    @GetMapping("/if-none-match")
    public ResponseEntity<Map<String, Object>> getIfNoneMatch(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam String etag) {
        return ResponseEntity.ok(service.getIfNoneMatch(bucket, key, etag));
    }

    @GetMapping("/if-modified-since")
    public ResponseEntity<Map<String, Object>> getIfModifiedSince(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam String since) {
        return ResponseEntity.ok(service.getIfModifiedSince(bucket, key, Instant.parse(since)));
    }

    @PostMapping("/if-not-exists")
    public ResponseEntity<Map<String, Object>> putIfNotExists(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam(defaultValue = "application/octet-stream") String contentType,
            @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.putIfNotExists(bucket, key, file.getBytes(), contentType));
    }

    @GetMapping("/etag")
    public ResponseEntity<Map<String, String>> getETag(
            @RequestParam String bucket,
            @RequestParam String key) {
        return ResponseEntity.ok(Map.of("eTag", service.getETag(bucket, key)));
    }
}
