package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.util.Map;

@Service
public class ObjectLockService {

    private final S3Client s3Client;

    public ObjectLockService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String createBucketWithObjectLock(String bucketName) {
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .objectLockEnabledForBucket(true)
                .build());
        return bucketName;
    }

    public void putDefaultRetention(String bucket, String mode, int days) {
        s3Client.putObjectLockConfiguration(PutObjectLockConfigurationRequest.builder()
                .bucket(bucket)
                .objectLockConfiguration(ObjectLockConfiguration.builder()
                        .objectLockEnabled(ObjectLockEnabled.ENABLED)
                        .rule(ObjectLockRule.builder()
                                .defaultRetention(DefaultRetention.builder()
                                        .mode(ObjectLockRetentionMode.fromValue(mode))
                                        .days(days)
                                        .build())
                                .build())
                        .build())
                .build());
    }

    public Map<String, String> getObjectLockConfiguration(String bucket) {
        GetObjectLockConfigurationResponse response = s3Client.getObjectLockConfiguration(
                GetObjectLockConfigurationRequest.builder().bucket(bucket).build());
        ObjectLockConfiguration config = response.objectLockConfiguration();

        if (config.rule() == null || config.rule().defaultRetention() == null) {
            return Map.of("enabled", config.objectLockEnabledAsString());
        }

        DefaultRetention retention = config.rule().defaultRetention();
        return Map.of(
                "enabled", config.objectLockEnabledAsString(),
                "mode", retention.modeAsString(),
                "days", String.valueOf(retention.days()));
    }

    public void putObjectRetention(String bucket, String key, String mode, Instant retainUntil) {
        s3Client.putObjectRetention(PutObjectRetentionRequest.builder()
                .bucket(bucket)
                .key(key)
                .retention(ObjectLockRetention.builder()
                        .mode(ObjectLockRetentionMode.fromValue(mode))
                        .retainUntilDate(retainUntil)
                        .build())
                .build());
    }

    public Map<String, String> getObjectRetention(String bucket, String key) {
        GetObjectRetentionResponse response = s3Client.getObjectRetention(
                GetObjectRetentionRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build());
        ObjectLockRetention retention = response.retention();
        return Map.of(
                "mode", retention.modeAsString(),
                "retainUntilDate", retention.retainUntilDate().toString());
    }

    public void putLegalHold(String bucket, String key, boolean enabled) {
        s3Client.putObjectLegalHold(PutObjectLegalHoldRequest.builder()
                .bucket(bucket)
                .key(key)
                .legalHold(ObjectLockLegalHold.builder()
                        .status(enabled ? ObjectLockLegalHoldStatus.ON : ObjectLockLegalHoldStatus.OFF)
                        .build())
                .build());
    }

    public Map<String, String> getLegalHold(String bucket, String key) {
        GetObjectLegalHoldResponse response = s3Client.getObjectLegalHold(
                GetObjectLegalHoldRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build());
        return Map.of("status", response.legalHold().statusAsString());
    }
}
