package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.EncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/encryption")
public class EncryptionController {

    private final EncryptionService service;

    public EncryptionController(EncryptionService service) {
        this.service = service;
    }

    @PutMapping("/configure")
    public ResponseEntity<Void> configureDefaultEncryption(
            @RequestParam String bucket,
            @RequestParam(defaultValue = "AES256") String algorithm) {
        service.configureDefaultEncryption(bucket, algorithm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getEncryptionConfiguration(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getEncryptionConfiguration(bucket));
    }

    @DeleteMapping("/config")
    public ResponseEntity<Void> deleteEncryptionConfiguration(@RequestParam String bucket) {
        service.deleteEncryptionConfiguration(bucket);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> putEncryptedObject(
            @RequestParam String bucket, @RequestParam String key,
            @RequestParam(defaultValue = "AES256") String algorithm,
            @RequestParam MultipartFile file) throws IOException {
        String encryption = service.putEncryptedObject(bucket, key, file.getBytes(), algorithm);
        return ResponseEntity.ok(Map.of("serverSideEncryption", encryption != null ? encryption : "none"));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getObjectEncryptionInfo(
            @RequestParam String bucket, @RequestParam String key) {
        return ResponseEntity.ok(service.getObjectEncryptionInfo(bucket, key));
    }
}
