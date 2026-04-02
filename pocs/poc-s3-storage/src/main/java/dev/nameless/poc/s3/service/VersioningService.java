package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Map;

@Service
public class VersioningService {

    private final S3Client s3Client;

    public VersioningService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void enableVersioning(String bucket) {
        s3Client.putBucketVersioning(PutBucketVersioningRequest.builder()
                .bucket(bucket)
                .versioningConfiguration(VersioningConfiguration.builder()
                        .status(BucketVersioningStatus.ENABLED)
                        .build())
                .build());
    }

    public void suspendVersioning(String bucket) {
        s3Client.putBucketVersioning(PutBucketVersioningRequest.builder()
                .bucket(bucket)
                .versioningConfiguration(VersioningConfiguration.builder()
                        .status(BucketVersioningStatus.SUSPENDED)
                        .build())
                .build());
    }

    public String getVersioningStatus(String bucket) {
        return s3Client.getBucketVersioning(GetBucketVersioningRequest.builder()
                .bucket(bucket)
                .build())
                .statusAsString();
    }

    public String putObjectVersioned(String bucket, String key, byte[] content, String contentType) {
        PutObjectResponse response = s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content));
        return response.versionId();
    }

    public byte[] getObjectVersion(String bucket, String key, String versionId) {
        return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .versionId(versionId)
                .build())
                .asByteArray();
    }

    public List<Map<String, String>> listObjectVersions(String bucket, String prefix) {
        ListObjectVersionsRequest.Builder builder = ListObjectVersionsRequest.builder()
                .bucket(bucket);
        if (prefix != null && !prefix.isEmpty()) {
            builder.prefix(prefix);
        }
        return s3Client.listObjectVersions(builder.build()).versions().stream()
                .map(v -> Map.of(
                        "key", v.key(),
                        "versionId", v.versionId(),
                        "isLatest", String.valueOf(v.isLatest()),
                        "lastModified", v.lastModified().toString(),
                        "size", String.valueOf(v.size())))
                .toList();
    }

    public void deleteObjectVersion(String bucket, String key, String versionId) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .versionId(versionId)
                .build());
    }
}
