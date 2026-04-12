package dev.nameless.poc.kmsencryption.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EncryptionService {

    private final KmsClient kmsClient;

    public EncryptionService(KmsClient kmsClient) {
        this.kmsClient = kmsClient;
    }

    public Map<String, String> encrypt(String keyId, String plaintext) {
        EncryptResponse response = kmsClient.encrypt(EncryptRequest.builder()
                .keyId(keyId)
                .plaintext(SdkBytes.fromUtf8String(plaintext))
                .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("ciphertextBlob", Base64.getEncoder().encodeToString(
                response.ciphertextBlob().asByteArray()));
        result.put("keyId", response.keyId());
        return result;
    }

    public Map<String, String> decrypt(String keyId, String ciphertextBase64) {
        byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertextBase64);

        DecryptResponse response = kmsClient.decrypt(DecryptRequest.builder()
                .keyId(keyId)
                .ciphertextBlob(SdkBytes.fromByteArray(ciphertextBytes))
                .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("plaintext", response.plaintext().asUtf8String());
        result.put("keyId", response.keyId());
        return result;
    }

    public Map<String, String> generateDataKey(String keyId) {
        GenerateDataKeyResponse response = kmsClient.generateDataKey(
                GenerateDataKeyRequest.builder()
                        .keyId(keyId)
                        .keySpec(DataKeySpec.AES_256)
                        .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("plaintextKey", Base64.getEncoder().encodeToString(
                response.plaintext().asByteArray()));
        result.put("ciphertextKey", Base64.getEncoder().encodeToString(
                response.ciphertextBlob().asByteArray()));
        result.put("keyId", response.keyId());
        return result;
    }

    public Map<String, String> envelopeEncrypt(String keyId, String plaintext) {
        Map<String, String> dataKey = generateDataKey(keyId);

        byte[] plaintextKeyBytes = Base64.getDecoder().decode(dataKey.get("plaintextKey"));

        try {
            SecretKeySpec aesKey = new SecretKeySpec(plaintextKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            Map<String, String> result = new LinkedHashMap<>();
            result.put("encryptedData", Base64.getEncoder().encodeToString(encryptedData));
            result.put("encryptedDataKey", dataKey.get("ciphertextKey"));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Envelope encryption failed", e);
        }
    }

    public Map<String, String> envelopeDecrypt(String keyId, String encryptedDataKeyBase64,
                                                String encryptedDataBase64) {
        byte[] encryptedDataKeyBytes = Base64.getDecoder().decode(encryptedDataKeyBase64);

        DecryptResponse decryptResponse = kmsClient.decrypt(DecryptRequest.builder()
                .keyId(keyId)
                .ciphertextBlob(SdkBytes.fromByteArray(encryptedDataKeyBytes))
                .build());

        byte[] plaintextKeyBytes = decryptResponse.plaintext().asByteArray();

        try {
            SecretKeySpec aesKey = new SecretKeySpec(plaintextKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedDataBase64));

            Map<String, String> result = new LinkedHashMap<>();
            result.put("plaintext", new String(decryptedData, StandardCharsets.UTF_8));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Envelope decryption failed", e);
        }
    }
}
