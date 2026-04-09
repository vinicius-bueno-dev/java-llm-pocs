package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.ChecksumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/checksums")
public class ChecksumController {

    private final ChecksumService service;

    public ChecksumController(ChecksumService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadWithChecksum(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam(defaultValue = "SHA256") String algorithm,
            @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.uploadWithChecksum(bucket, key, file.getBytes(), algorithm));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> getObjectChecksum(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam(defaultValue = "SHA256") String algorithm) {
        return ResponseEntity.ok(service.getObjectChecksum(bucket, key, algorithm));
    }

    @PostMapping("/upload-precalculated")
    public ResponseEntity<Map<String, String>> uploadWithPrecalculatedChecksum(
            @RequestParam String bucket,
            @RequestParam String key,
            @RequestParam String algorithm,
            @RequestParam String checksumValue,
            @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.uploadWithPrecalculatedChecksum(
                bucket, key, file.getBytes(), algorithm, checksumValue));
    }
}
