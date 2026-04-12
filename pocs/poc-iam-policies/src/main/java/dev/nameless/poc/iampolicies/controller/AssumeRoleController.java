package dev.nameless.poc.iampolicies.controller;

import dev.nameless.poc.iampolicies.service.AssumeRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

@RestController
@RequestMapping("/api/iam/assume-role")
public class AssumeRoleController {

    private final AssumeRoleService service;

    public AssumeRoleController(AssumeRoleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Credentials> assumeRole(
            @RequestParam String roleArn,
            @RequestParam String sessionName) {
        return ResponseEntity.ok(service.assumeRole(roleArn, sessionName));
    }

    @GetMapping("/caller-identity")
    public ResponseEntity<GetCallerIdentityResponse> callerIdentity() {
        return ResponseEntity.ok(service.getCallerIdentity());
    }
}
