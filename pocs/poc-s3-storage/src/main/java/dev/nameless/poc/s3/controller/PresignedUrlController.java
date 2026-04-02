package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.PresignedUrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/presigned")
public class PresignedUrlController {

    private final PresignedUrlService service;

    public PresignedUrlController(PresignedUrlService service) {
        this.service = service;
    }

    @GetMapping("/download-url")
    public ResponseEntity<Map<String, String>> generatePresignedGetUrl(
            @RequestParam String bucket, @RequestParam String key,
            @RequestParam(defaultValue = "15") int expirationMinutes) {
        String url = service.generatePresignedGetUrl(bucket, key, Duration.ofMinutes(expirationMinutes));
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/upload-url")
    public ResponseEntity<Map<String, String>> generatePresignedPutUrl(
            @RequestParam String bucket, @RequestParam String key,
            @RequestParam(defaultValue = "application/octet-stream") String contentType,
            @RequestParam(defaultValue = "15") int expirationMinutes) {
        String url = service.generatePresignedPutUrl(bucket, key, contentType, Duration.ofMinutes(expirationMinutes));
        return ResponseEntity.ok(Map.of("url", url));
    }
}
