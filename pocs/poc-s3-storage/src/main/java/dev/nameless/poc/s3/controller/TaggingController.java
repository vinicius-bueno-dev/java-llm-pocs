package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.TaggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/s3/tags")
public class TaggingController {

    private final TaggingService service;

    public TaggingController(TaggingService service) {
        this.service = service;
    }

    @PutMapping("/bucket")
    public ResponseEntity<Void> setBucketTags(@RequestParam String bucket, @RequestBody Map<String, String> tags) {
        service.setBucketTags(bucket, tags);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bucket")
    public ResponseEntity<Map<String, String>> getBucketTags(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getBucketTags(bucket));
    }

    @DeleteMapping("/bucket")
    public ResponseEntity<Void> deleteBucketTags(@RequestParam String bucket) {
        service.deleteBucketTags(bucket);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/object")
    public ResponseEntity<Void> setObjectTags(
            @RequestParam String bucket, @RequestParam String key, @RequestBody Map<String, String> tags) {
        service.setObjectTags(bucket, key, tags);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/object")
    public ResponseEntity<Map<String, String>> getObjectTags(
            @RequestParam String bucket, @RequestParam String key) {
        return ResponseEntity.ok(service.getObjectTags(bucket, key));
    }

    @DeleteMapping("/object")
    public ResponseEntity<Void> deleteObjectTags(@RequestParam String bucket, @RequestParam String key) {
        service.deleteObjectTags(bucket, key);
        return ResponseEntity.noContent().build();
    }
}
