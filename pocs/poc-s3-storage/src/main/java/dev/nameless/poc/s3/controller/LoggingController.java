package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.LoggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/s3/logging")
public class LoggingController {

    private final LoggingService service;

    public LoggingController(LoggingService service) {
        this.service = service;
    }

    @PutMapping("/configure")
    public ResponseEntity<Void> configureAccessLogging(
            @RequestParam String sourceBucket,
            @RequestParam String targetBucket,
            @RequestParam(defaultValue = "access-logs/") String targetPrefix) {
        service.configureAccessLogging(sourceBucket, targetBucket, targetPrefix);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getLoggingConfiguration(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getLoggingConfiguration(bucket));
    }

    @DeleteMapping("/config")
    public ResponseEntity<Void> deleteLoggingConfiguration(@RequestParam String bucket) {
        service.deleteLoggingConfiguration(bucket);
        return ResponseEntity.noContent().build();
    }
}
