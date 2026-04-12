package dev.nameless.poc.kmsencryption.controller;

import dev.nameless.poc.kmsencryption.dto.EncryptRequestDto;
import dev.nameless.poc.kmsencryption.dto.EnvelopeEncryptDto;
import dev.nameless.poc.kmsencryption.service.EncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/kms/encrypt")
public class EncryptionController {

    private final EncryptionService encryptionService;

    public EncryptionController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> encrypt(@RequestBody EncryptRequestDto dto) {
        return ResponseEntity.ok(encryptionService.encrypt(dto.keyId(), dto.plaintext()));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, String>> decrypt(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(encryptionService.decrypt(
                body.get("keyId"), body.get("ciphertextBlob")));
    }

    @PostMapping("/generate-data-key")
    public ResponseEntity<Map<String, String>> generateDataKey(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(encryptionService.generateDataKey(body.get("keyId")));
    }

    @PostMapping("/envelope-encrypt")
    public ResponseEntity<Map<String, String>> envelopeEncrypt(@RequestBody EnvelopeEncryptDto dto) {
        return ResponseEntity.ok(encryptionService.envelopeEncrypt(dto.keyId(), dto.plaintext()));
    }

    @PostMapping("/envelope-decrypt")
    public ResponseEntity<Map<String, String>> envelopeDecrypt(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(encryptionService.envelopeDecrypt(
                body.get("keyId"),
                body.get("encryptedDataKey"),
                body.get("encryptedData")));
    }
}
