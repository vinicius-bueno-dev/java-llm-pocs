package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.BucketOperationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/buckets")
public class BucketOperationsController {

    private final BucketOperationsService service;

    public BucketOperationsController(BucketOperationsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createBucket(@RequestParam String name) {
        return ResponseEntity.ok(Map.of("bucket", service.createBucket(name)));
    }

    @GetMapping
    public ResponseEntity<List<String>> listBuckets() {
        return ResponseEntity.ok(service.listBuckets());
    }

    @GetMapping("/{name}/exists")
    public ResponseEntity<Map<String, Boolean>> bucketExists(@PathVariable String name) {
        return ResponseEntity.ok(Map.of("exists", service.bucketExists(name)));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteBucket(@PathVariable String name) {
        service.deleteBucket(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{name}/location")
    public ResponseEntity<Map<String, String>> getBucketLocation(@PathVariable String name) {
        return ResponseEntity.ok(Map.of("location", service.getBucketLocation(name)));
    }
}
