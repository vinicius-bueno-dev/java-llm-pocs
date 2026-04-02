package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.PolicyAndAclService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/s3/policies")
public class PolicyAndAclController {

    private final PolicyAndAclService service;

    public PolicyAndAclController(PolicyAndAclService service) {
        this.service = service;
    }

    @PutMapping("/policy")
    public ResponseEntity<Void> setBucketPolicy(@RequestParam String bucket, @RequestBody String policyJson) {
        service.setBucketPolicy(bucket, policyJson);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/policy")
    public ResponseEntity<String> getBucketPolicy(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getBucketPolicy(bucket));
    }

    @DeleteMapping("/policy")
    public ResponseEntity<Void> deleteBucketPolicy(@RequestParam String bucket) {
        service.deleteBucketPolicy(bucket);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/acl/bucket")
    public ResponseEntity<Void> setBucketAcl(@RequestParam String bucket, @RequestParam String cannedAcl) {
        service.setBucketAcl(bucket, cannedAcl);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/acl/bucket")
    public ResponseEntity<Map<String, Object>> getBucketAcl(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getBucketAcl(bucket));
    }

    @PutMapping("/acl/object")
    public ResponseEntity<Void> setObjectAcl(
            @RequestParam String bucket, @RequestParam String key, @RequestParam String cannedAcl) {
        service.setObjectAcl(bucket, key, cannedAcl);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/public-access-block")
    public ResponseEntity<Void> setPublicAccessBlock(
            @RequestParam String bucket, @RequestBody Map<String, Boolean> config) {
        service.setPublicAccessBlock(bucket,
                config.getOrDefault("blockPublicAcls", true),
                config.getOrDefault("blockPublicPolicy", true),
                config.getOrDefault("ignorePublicAcls", true),
                config.getOrDefault("restrictPublicBuckets", true));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public-access-block")
    public ResponseEntity<Map<String, Boolean>> getPublicAccessBlock(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getPublicAccessBlock(bucket));
    }
}
