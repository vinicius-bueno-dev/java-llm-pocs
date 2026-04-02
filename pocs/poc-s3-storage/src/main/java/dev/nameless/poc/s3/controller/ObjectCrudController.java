package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.ObjectCrudService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/objects")
public class ObjectCrudController {

    private final ObjectCrudService service;

    public ObjectCrudController(ObjectCrudService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> putObject(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam MultipartFile file) throws IOException {
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String eTag = service.putObject(bucket, key, file.getBytes(), contentType);
        return ResponseEntity.ok(Map.of("eTag", eTag, "key", key));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> getObject(@RequestParam String bucket, @RequestParam String key) {
        byte[] content = service.getObject(bucket, key);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, String>> headObject(
            @RequestParam String bucket, @RequestParam String key) {
        return ResponseEntity.ok(service.headObject(bucket, key));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteObject(@RequestParam String bucket, @RequestParam String key) {
        service.deleteObject(bucket, key);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Map<String, Integer>> deleteObjects(
            @RequestParam String bucket, @RequestBody List<String> keys) {
        return ResponseEntity.ok(Map.of("deleted", service.deleteObjects(bucket, keys)));
    }

    @PostMapping("/copy")
    public ResponseEntity<Map<String, String>> copyObject(
            @RequestParam String sourceBucket, @RequestParam String sourceKey,
            @RequestParam String destBucket, @RequestParam String destKey) {
        String eTag = service.copyObject(sourceBucket, sourceKey, destBucket, destKey);
        return ResponseEntity.ok(Map.of("eTag", eTag));
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listObjects(
            @RequestParam String bucket,
            @RequestParam(required = false, defaultValue = "") String prefix,
            @RequestParam(required = false, defaultValue = "100") int maxKeys) {
        return ResponseEntity.ok(service.listObjects(bucket, prefix, maxKeys));
    }
}
