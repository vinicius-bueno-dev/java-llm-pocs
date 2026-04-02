package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Map;

@Service
public class LoggingService {

    private final S3Client s3Client;

    public LoggingService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void configureAccessLogging(String sourceBucket, String targetBucket, String targetPrefix) {
        s3Client.putBucketLogging(PutBucketLoggingRequest.builder()
                .bucket(sourceBucket)
                .bucketLoggingStatus(BucketLoggingStatus.builder()
                        .loggingEnabled(LoggingEnabled.builder()
                                .targetBucket(targetBucket)
                                .targetPrefix(targetPrefix)
                                .build())
                        .build())
                .build());
    }

    public Map<String, String> getLoggingConfiguration(String bucket) {
        GetBucketLoggingResponse response = s3Client.getBucketLogging(
                GetBucketLoggingRequest.builder().bucket(bucket).build());
        if (response.loggingEnabled() == null) {
            return Map.of("status", "disabled");
        }
        return Map.of(
                "status", "enabled",
                "targetBucket", response.loggingEnabled().targetBucket(),
                "targetPrefix", response.loggingEnabled().targetPrefix());
    }

    public void deleteLoggingConfiguration(String bucket) {
        s3Client.putBucketLogging(PutBucketLoggingRequest.builder()
                .bucket(bucket)
                .bucketLoggingStatus(BucketLoggingStatus.builder().build())
                .build());
    }
}
