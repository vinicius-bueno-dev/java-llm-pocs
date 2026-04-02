package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.MultipartUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/multipart")
public class MultipartUploadController {

    private final MultipartUploadService service;

    public MultipartUploadController(MultipartUploadService service) {
        this.service = service;
    }

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, String>> initiateMultipartUpload(
            @RequestParam String bucket, @RequestParam String key,
            @RequestParam(defaultValue = "application/octet-stream") String contentType) {
        String uploadId = service.initiateMultipartUpload(bucket, key, contentType);
        return ResponseEntity.ok(Map.of("uploadId", uploadId));
    }

    @PutMapping("/upload-part")
    public ResponseEntity<Map<String, String>> uploadPart(
            @RequestParam String bucket, @RequestParam String key,
            @RequestParam String uploadId, @RequestParam int partNumber,
            @RequestParam MultipartFile part) throws IOException {
        String eTag = service.uploadPart(bucket, key, uploadId, partNumber, part.getBytes());
        return ResponseEntity.ok(Map.of("eTag", eTag, "partNumber", String.valueOf(partNumber)));
    }

    @PostMapping("/complete")
    public ResponseEntity<Map<String, String>> completeMultipartUpload(
            @RequestParam String bucket, @RequestParam String key,
            @RequestParam String uploadId,
            @RequestBody List<Map<String, String>> parts) {
        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(Integer.parseInt(p.get("partNumber")))
                        .eTag(p.get("eTag"))
                        .build())
                .toList();
        String eTag = service.completeMultipartUpload(bucket, key, uploadId, completedParts);
        return ResponseEntity.ok(Map.of("eTag", eTag));
    }

    @DeleteMapping("/abort")
    public ResponseEntity<Void> abortMultipartUpload(
            @RequestParam String bucket, @RequestParam String key, @RequestParam String uploadId) {
        service.abortMultipartUpload(bucket, key, uploadId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> listMultipartUploads(@RequestParam String bucket) {
        return ResponseEntity.ok(service.listMultipartUploads(bucket));
    }
}
