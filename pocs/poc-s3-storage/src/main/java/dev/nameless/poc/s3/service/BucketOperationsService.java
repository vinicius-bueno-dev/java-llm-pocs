package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Service
public class BucketOperationsService {

    private final S3Client s3Client;

    public BucketOperationsService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String createBucket(String bucketName) {
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .build());
        return bucketName;
    }

    public List<String> listBuckets() {
        return s3Client.listBuckets().buckets().stream()
                .map(Bucket::name)
                .toList();
    }

    public boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    public void deleteBucket(String bucketName) {
        s3Client.deleteBucket(DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build());
    }

    public String getBucketLocation(String bucketName) {
        return s3Client.getBucketLocation(GetBucketLocationRequest.builder()
                .bucket(bucketName)
                .build())
                .locationConstraintAsString();
    }
}
