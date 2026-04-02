package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class PresignedUrlService {

    private final S3Presigner s3Presigner;

    public PresignedUrlService(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public String generatePresignedGetUrl(String bucket, String key, Duration expiration) {
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())
                .build())
                .url()
                .toString();
    }

    public String generatePresignedPutUrl(String bucket, String key,
                                          String contentType, Duration expiration) {
        return s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build())
                .build())
                .url()
                .toString();
    }
}
