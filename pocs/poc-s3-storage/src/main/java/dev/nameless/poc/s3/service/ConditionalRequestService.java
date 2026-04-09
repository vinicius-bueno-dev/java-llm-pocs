package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.util.Map;

@Service
public class ConditionalRequestService {

    private final S3Client s3Client;

    public ConditionalRequestService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public Map<String, Object> getIfMatch(String bucket, String key, String etag) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .ifMatch(etag)
                            .build());
            return Map.of(
                    "matched", true,
                    "eTag", response.response().eTag(),
                    "contentLength", response.response().contentLength(),
                    "content", new String(response.asByteArray()));
        } catch (S3Exception e) {
            if (e.statusCode() == 412) {
                return Map.of("matched", false, "reason", "Precondition Failed — ETag does not match");
            }
            throw e;
        }
    }

    public Map<String, Object> getIfNoneMatch(String bucket, String key, String etag) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .ifNoneMatch(etag)
                            .build());
            return Map.of(
                    "modified", true,
                    "eTag", response.response().eTag(),
                    "content", new String(response.asByteArray()));
        } catch (S3Exception e) {
            if (e.statusCode() == 304) {
                return Map.of("modified", false, "reason", "Not Modified — ETag still matches");
            }
            throw e;
        }
    }

    public Map<String, Object> getIfModifiedSince(String bucket, String key, Instant since) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .ifModifiedSince(since)
                            .build());
            return Map.of(
                    "modified", true,
                    "eTag", response.response().eTag(),
                    "lastModified", response.response().lastModified().toString(),
                    "content", new String(response.asByteArray()));
        } catch (S3Exception e) {
            if (e.statusCode() == 304) {
                return Map.of("modified", false, "reason", "Not Modified since " + since);
            }
            throw e;
        }
    }

    public Map<String, Object> putIfNotExists(String bucket, String key, byte[] content, String contentType) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return Map.of("created", false, "reason", "Object already exists");
        } catch (NoSuchKeyException e) {
            PutObjectResponse response = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));
            return Map.of(
                    "created", true,
                    "eTag", response.eTag(),
                    "key", key);
        }
    }

    public String getETag(String bucket, String key) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        return response.eTag();
    }
}
