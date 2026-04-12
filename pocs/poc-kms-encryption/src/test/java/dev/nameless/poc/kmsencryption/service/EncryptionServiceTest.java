package dev.nameless.poc.kmsencryption.service;

import dev.nameless.poc.kmsencryption.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptionServiceTest extends AbstractLocalStackTest {

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private KeyManagementService keyManagementService;

    private String keyId;

    @BeforeEach
    void createTestKey() {
        Map<String, String> key = keyManagementService.createKey("encryption-test-key", null);
        keyId = key.get("keyId");
    }

    @Test
    void shouldEncryptAndDecrypt() {
        String original = "Hello, KMS!";

        Map<String, String> encrypted = encryptionService.encrypt(keyId, original);
        assertThat(encrypted).containsKeys("ciphertextBlob", "keyId");
        assertThat(encrypted.get("ciphertextBlob")).isNotBlank();

        Map<String, String> decrypted = encryptionService.decrypt(keyId, encrypted.get("ciphertextBlob"));
        assertThat(decrypted.get("plaintext")).isEqualTo(original);
    }

    @Test
    void shouldGenerateDataKey() {
        Map<String, String> dataKey = encryptionService.generateDataKey(keyId);

        assertThat(dataKey).containsKeys("plaintextKey", "ciphertextKey", "keyId");
        assertThat(dataKey.get("plaintextKey")).isNotBlank();
        assertThat(dataKey.get("ciphertextKey")).isNotBlank();
        assertThat(dataKey.get("plaintextKey")).isNotEqualTo(dataKey.get("ciphertextKey"));
    }

    @Test
    void shouldEnvelopeEncryptAndDecrypt() {
        String original = "Sensitive data for envelope encryption";

        Map<String, String> envelopeResult = encryptionService.envelopeEncrypt(keyId, original);
        assertThat(envelopeResult).containsKeys("encryptedData", "encryptedDataKey");

        Map<String, String> decrypted = encryptionService.envelopeDecrypt(
                keyId,
                envelopeResult.get("encryptedDataKey"),
                envelopeResult.get("encryptedData"));

        assertThat(decrypted.get("plaintext")).isEqualTo(original);
    }

    @Test
    void shouldEncryptAndDecryptLargePayload() {
        String largePayload = "A".repeat(4096);

        Map<String, String> envelopeResult = encryptionService.envelopeEncrypt(keyId, largePayload);
        Map<String, String> decrypted = encryptionService.envelopeDecrypt(
                keyId,
                envelopeResult.get("encryptedDataKey"),
                envelopeResult.get("encryptedData"));

        assertThat(decrypted.get("plaintext")).isEqualTo(largePayload);
    }
}
