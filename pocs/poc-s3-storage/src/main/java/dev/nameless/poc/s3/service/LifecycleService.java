package dev.nameless.poc.s3.service;

import dev.nameless.poc.s3.dto.LifecycleRuleDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LifecycleService {

    private final S3Client s3Client;

    public LifecycleService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void setLifecycleRules(String bucket, List<LifecycleRuleDto> rules) {
        List<LifecycleRule> s3Rules = rules.stream()
                .map(this::toLifecycleRule)
                .toList();

        s3Client.putBucketLifecycleConfiguration(PutBucketLifecycleConfigurationRequest.builder()
                .bucket(bucket)
                .lifecycleConfiguration(BucketLifecycleConfiguration.builder()
                        .rules(s3Rules)
                        .build())
                .build());
    }

    public List<Map<String, Object>> getLifecycleRules(String bucket) {
        GetBucketLifecycleConfigurationResponse response = s3Client.getBucketLifecycleConfiguration(
                GetBucketLifecycleConfigurationRequest.builder().bucket(bucket).build());

        return response.rules().stream()
                .map(r -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", r.id());
                    map.put("status", r.statusAsString());
                    map.put("prefix", r.filter() != null ? r.filter().prefix() : "");
                    if (r.expiration() != null && r.expiration().days() != null) {
                        map.put("expirationDays", r.expiration().days());
                    }
                    if (r.transitions() != null && !r.transitions().isEmpty()) {
                        map.put("transitions", r.transitions().stream()
                                .map(t -> Map.of(
                                        "days", t.days(),
                                        "storageClass", t.storageClassAsString()))
                                .toList());
                    }
                    return map;
                })
                .toList();
    }

    public void deleteLifecycleRules(String bucket) {
        s3Client.deleteBucketLifecycle(
                DeleteBucketLifecycleRequest.builder()
                        .bucket(bucket)
                        .build());
    }

    private LifecycleRule toLifecycleRule(LifecycleRuleDto dto) {
        LifecycleRule.Builder builder = LifecycleRule.builder()
                .id(dto.id())
                .status(ExpirationStatus.ENABLED)
                .filter(LifecycleRuleFilter.builder().prefix(dto.prefix()).build());

        if (dto.expirationDays() > 0) {
            builder.expiration(LifecycleExpiration.builder().days(dto.expirationDays()).build());
        }

        if (dto.transitionStorageClass() != null && !dto.transitionStorageClass().isEmpty()) {
            List<Transition> transitions = new ArrayList<>();
            transitions.add(Transition.builder()
                    .days(dto.transitionDays())
                    .storageClass(TransitionStorageClass.fromValue(dto.transitionStorageClass()))
                    .build());
            builder.transitions(transitions);
        }

        return builder.build();
    }
}
