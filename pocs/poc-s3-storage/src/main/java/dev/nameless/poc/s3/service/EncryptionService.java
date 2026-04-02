package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Map;

@Service
public class EncryptionService {

    private final S3Client s3Client;

    public EncryptionService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void configureDefaultEncryption(String bucket, String algorithm) {
        s3Client.putBucketEncryption(PutBucketEncryptionRequest.builder()
                .bucket(bucket)
                .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
                        .rules(ServerSideEncryptionRule.builder()
                                .applyServerSideEncryptionByDefault(
                                        ServerSideEncryptionByDefault.builder()
                                                .sseAlgorithm(ServerSideEncryption.fromValue(algorithm))
                                                .build())
                                .bucketKeyEnabled(true)
                                .build())
                        .build())
                .build());
    }

    public Map<String, String> getEncryptionConfiguration(String bucket) {
        GetBucketEncryptionResponse response = s3Client.getBucketEncryption(
                GetBucketEncryptionRequest.builder().bucket(bucket).build());
        ServerSideEncryptionRule rule = response.serverSideEncryptionConfiguration().rules().getFirst();
        return Map.of(
                "algorithm", rule.applyServerSideEncryptionByDefault().sseAlgorithmAsString(),
                "bucketKeyEnabled", String.valueOf(rule.bucketKeyEnabled()));
    }

    public void deleteEncryptionConfiguration(String bucket) {
        s3Client.deleteBucketEncryption(DeleteBucketEncryptionRequest.builder()
                .bucket(bucket)
                .build());
    }

    public String putEncryptedObject(String bucket, String key, byte[] content, String algorithm) {
        PutObjectResponse response = s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .serverSideEncryption(ServerSideEncryption.fromValue(algorithm))
                        .build(),
                RequestBody.fromBytes(content));
        return response.serverSideEncryptionAsString();
    }

    public Map<String, String> getObjectEncryptionInfo(String bucket, String key) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        return Map.of(
                "serverSideEncryption", String.valueOf(response.serverSideEncryptionAsString()),
                "bucketKeyEnabled", String.valueOf(response.bucketKeyEnabled()));
    }
}
