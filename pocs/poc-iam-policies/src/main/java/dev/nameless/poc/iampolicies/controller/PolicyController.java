package dev.nameless.poc.iampolicies.controller;

import dev.nameless.poc.iampolicies.dto.CreatePolicyDto;
import dev.nameless.poc.iampolicies.service.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.PolicyVersion;

import java.util.List;

@RestController
@RequestMapping("/api/iam/policies")
public class PolicyController {

    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Policy> create(@RequestBody CreatePolicyDto dto) {
        return ResponseEntity.ok(service.createPolicy(dto));
    }

    @GetMapping
    public ResponseEntity<List<Policy>> list() {
        return ResponseEntity.ok(service.listPolicies());
    }

    @GetMapping("/by-arn")
    public ResponseEntity<Policy> get(@RequestParam String policyArn) {
        return ResponseEntity.ok(service.getPolicy(policyArn));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String policyArn) {
        service.deletePolicy(policyArn);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/version")
    public ResponseEntity<PolicyVersion> getVersion(
            @RequestParam String policyArn,
            @RequestParam String versionId) {
        return ResponseEntity.ok(service.getPolicyVersion(policyArn, versionId));
    }
}
