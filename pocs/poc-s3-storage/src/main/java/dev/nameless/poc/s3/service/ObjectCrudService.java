package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Map;

@Service
public class ObjectCrudService {

    private final S3Client s3Client;

    public ObjectCrudService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String putObject(String bucket, String key, byte[] content, String contentType) {
        PutObjectResponse response = s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content));
        return response.eTag();
    }

    public byte[] getObject(String bucket, String key) {
        return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())
                .asByteArray();
    }

    public Map<String, String> headObject(String bucket, String key) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        return Map.of(
                "contentType", response.contentType(),
                "contentLength", String.valueOf(response.contentLength()),
                "eTag", response.eTag(),
                "lastModified", response.lastModified().toString());
    }

    public void deleteObject(String bucket, String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public int deleteObjects(String bucket, List<String> keys) {
        List<ObjectIdentifier> identifiers = keys.stream()
                .map(k -> ObjectIdentifier.builder().key(k).build())
                .toList();
        DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(identifiers).build())
                .build());
        return response.deleted().size();
    }

    public String copyObject(String sourceBucket, String sourceKey,
                             String destBucket, String destKey) {
        CopyObjectResponse response = s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(destBucket)
                .destinationKey(destKey)
                .build());
        return response.copyObjectResult().eTag();
    }

    public List<String> listObjects(String bucket, String prefix, int maxKeys) {
        ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                .bucket(bucket)
                .maxKeys(maxKeys);
        if (prefix != null && !prefix.isEmpty()) {
            builder.prefix(prefix);
        }
        return s3Client.listObjectsV2(builder.build()).contents().stream()
                .map(S3Object::key)
                .toList();
    }
}
