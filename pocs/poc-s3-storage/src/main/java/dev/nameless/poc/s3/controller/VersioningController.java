package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.VersioningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/versioning")
public class VersioningController {

    private final VersioningService service;

    public VersioningController(VersioningService service) {
        this.service = service;
    }

    @PutMapping("/enable")
    public ResponseEntity<Void> enableVersioning(@RequestParam String bucket) {
        service.enableVersioning(bucket);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/suspend")
    public ResponseEntity<Void> suspendVersioning(@RequestParam String bucket) {
        service.suspendVersioning(bucket);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getVersioningStatus(@RequestParam String bucket) {
        return ResponseEntity.ok(Map.of("status", service.getVersioningStatus(bucket)));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> putObjectVersioned(
            @RequestParam String bucket, @RequestParam String key,
            @RequestParam MultipartFile file) throws IOException {
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String versionId = service.putObjectVersioned(bucket, key, file.getBytes(), contentType);
        return ResponseEntity.ok(Map.of("versionId", versionId != null ? versionId : "null"));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> getObjectVersion(
            @RequestParam String bucket, @RequestParam String key, @RequestParam String versionId) {
        return ResponseEntity.ok(service.getObjectVersion(bucket, key, versionId));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> listObjectVersions(
            @RequestParam String bucket,
            @RequestParam(required = false, defaultValue = "") String prefix) {
        return ResponseEntity.ok(service.listObjectVersions(bucket, prefix));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteObjectVersion(
            @RequestParam String bucket, @RequestParam String key, @RequestParam String versionId) {
        service.deleteObjectVersion(bucket, key, versionId);
        return ResponseEntity.noContent().build();
    }
}
