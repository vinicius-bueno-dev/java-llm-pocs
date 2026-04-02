package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaggingService {

    private final S3Client s3Client;

    public TaggingService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void setBucketTags(String bucket, Map<String, String> tags) {
        s3Client.putBucketTagging(PutBucketTaggingRequest.builder()
                .bucket(bucket)
                .tagging(Tagging.builder()
                        .tagSet(tags.entrySet().stream()
                                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                                .toList())
                        .build())
                .build());
    }

    public Map<String, String> getBucketTags(String bucket) {
        return s3Client.getBucketTagging(GetBucketTaggingRequest.builder()
                .bucket(bucket)
                .build())
                .tagSet().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value));
    }

    public void deleteBucketTags(String bucket) {
        s3Client.deleteBucketTagging(DeleteBucketTaggingRequest.builder()
                .bucket(bucket)
                .build());
    }

    public void setObjectTags(String bucket, String key, Map<String, String> tags) {
        s3Client.putObjectTagging(PutObjectTaggingRequest.builder()
                .bucket(bucket)
                .key(key)
                .tagging(Tagging.builder()
                        .tagSet(tags.entrySet().stream()
                                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                                .toList())
                        .build())
                .build());
    }

    public Map<String, String> getObjectTags(String bucket, String key) {
        return s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())
                .tagSet().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value));
    }

    public void deleteObjectTags(String bucket, String key) {
        s3Client.deleteObjectTagging(DeleteObjectTaggingRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }
}
