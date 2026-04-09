package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChecksumService {

    private final S3Client s3Client;

    public ChecksumService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public Map<String, String> uploadWithChecksum(String bucket, String key, byte[] content, String algorithm) {
        ChecksumAlgorithm checksumAlg = ChecksumAlgorithm.fromValue(algorithm);

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .checksumAlgorithm(checksumAlg);

        PutObjectResponse response = s3Client.putObject(requestBuilder.build(),
                RequestBody.fromBytes(content));

        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("algorithm", algorithm);
        result.put("eTag", response.eTag());

        switch (checksumAlg) {
            case CRC32 -> result.put("checksum", nullSafe(response.checksumCRC32()));
            case CRC32_C -> result.put("checksum", nullSafe(response.checksumCRC32C()));
            case SHA1 -> result.put("checksum", nullSafe(response.checksumSHA1()));
            case SHA256 -> result.put("checksum", nullSafe(response.checksumSHA256()));
            default -> result.put("checksum", "unsupported");
        }

        return result;
    }

    public Map<String, String> getObjectChecksum(String bucket, String key, String algorithm) {
        ChecksumAlgorithm checksumAlg = ChecksumAlgorithm.fromValue(algorithm);

        GetObjectAttributesResponse response = s3Client.getObjectAttributes(
                GetObjectAttributesRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .objectAttributes(ObjectAttributes.CHECKSUM, ObjectAttributes.OBJECT_SIZE)
                        .build());

        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("objectSize", String.valueOf(response.objectSize()));

        if (response.checksum() != null) {
            Checksum checksum = response.checksum();
            switch (checksumAlg) {
                case CRC32 -> result.put("checksum", nullSafe(checksum.checksumCRC32()));
                case CRC32_C -> result.put("checksum", nullSafe(checksum.checksumCRC32C()));
                case SHA1 -> result.put("checksum", nullSafe(checksum.checksumSHA1()));
                case SHA256 -> result.put("checksum", nullSafe(checksum.checksumSHA256()));
                default -> result.put("checksum", "unsupported");
            }
        }

        return result;
    }

    public Map<String, String> uploadWithPrecalculatedChecksum(String bucket, String key, byte[] content,
                                                                String algorithm, String checksumValue) {
        ChecksumAlgorithm checksumAlg = ChecksumAlgorithm.fromValue(algorithm);

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .checksumAlgorithm(checksumAlg);

        switch (checksumAlg) {
            case CRC32 -> requestBuilder.checksumCRC32(checksumValue);
            case CRC32_C -> requestBuilder.checksumCRC32C(checksumValue);
            case SHA1 -> requestBuilder.checksumSHA1(checksumValue);
            case SHA256 -> requestBuilder.checksumSHA256(checksumValue);
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }

        PutObjectResponse response = s3Client.putObject(requestBuilder.build(),
                RequestBody.fromBytes(content));

        return Map.of(
                "key", key,
                "algorithm", algorithm,
                "eTag", response.eTag(),
                "validated", "true");
    }

    private String nullSafe(String value) {
        return value != null ? value : "not-available";
    }
}
