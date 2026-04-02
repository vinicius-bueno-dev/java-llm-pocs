package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Map;

@Service
public class PolicyAndAclService {

    private final S3Client s3Client;

    public PolicyAndAclService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void setBucketPolicy(String bucket, String policyJson) {
        s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                .bucket(bucket)
                .policy(policyJson)
                .build());
    }

    public String getBucketPolicy(String bucket) {
        return s3Client.getBucketPolicy(GetBucketPolicyRequest.builder()
                .bucket(bucket)
                .build())
                .policy();
    }

    public void deleteBucketPolicy(String bucket) {
        s3Client.deleteBucketPolicy(DeleteBucketPolicyRequest.builder()
                .bucket(bucket)
                .build());
    }

    public void setBucketAcl(String bucket, String cannedAcl) {
        s3Client.putBucketAcl(PutBucketAclRequest.builder()
                .bucket(bucket)
                .acl(BucketCannedACL.fromValue(cannedAcl))
                .build());
    }

    public Map<String, Object> getBucketAcl(String bucket) {
        GetBucketAclResponse response = s3Client.getBucketAcl(
                GetBucketAclRequest.builder().bucket(bucket).build());
        List<Map<String, String>> grants = response.grants().stream()
                .map(g -> Map.of(
                        "permission", g.permissionAsString(),
                        "grantee", g.grantee().displayName() != null
                                ? g.grantee().displayName()
                                : g.grantee().uri() != null ? g.grantee().uri() : "unknown"))
                .toList();
        return Map.of(
                "owner", response.owner().displayName() != null ? response.owner().displayName() : "unknown",
                "grants", grants);
    }

    public void setObjectAcl(String bucket, String key, String cannedAcl) {
        s3Client.putObjectAcl(PutObjectAclRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl(ObjectCannedACL.fromValue(cannedAcl))
                .build());
    }

    public void setPublicAccessBlock(String bucket, boolean blockPublicAcls,
                                     boolean blockPublicPolicy, boolean ignorePublicAcls,
                                     boolean restrictPublicBuckets) {
        s3Client.putPublicAccessBlock(PutPublicAccessBlockRequest.builder()
                .bucket(bucket)
                .publicAccessBlockConfiguration(PublicAccessBlockConfiguration.builder()
                        .blockPublicAcls(blockPublicAcls)
                        .blockPublicPolicy(blockPublicPolicy)
                        .ignorePublicAcls(ignorePublicAcls)
                        .restrictPublicBuckets(restrictPublicBuckets)
                        .build())
                .build());
    }

    public Map<String, Boolean> getPublicAccessBlock(String bucket) {
        GetPublicAccessBlockResponse response = s3Client.getPublicAccessBlock(
                GetPublicAccessBlockRequest.builder().bucket(bucket).build());
        PublicAccessBlockConfiguration config = response.publicAccessBlockConfiguration();
        return Map.of(
                "blockPublicAcls", config.blockPublicAcls(),
                "blockPublicPolicy", config.blockPublicPolicy(),
                "ignorePublicAcls", config.ignorePublicAcls(),
                "restrictPublicBuckets", config.restrictPublicBuckets());
    }
}
