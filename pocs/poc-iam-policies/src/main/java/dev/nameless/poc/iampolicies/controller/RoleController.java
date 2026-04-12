package dev.nameless.poc.iampolicies.controller;

import dev.nameless.poc.iampolicies.dto.CreateRoleDto;
import dev.nameless.poc.iampolicies.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.Role;

import java.util.List;

@RestController
@RequestMapping("/api/iam/roles")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Role> create(@RequestBody CreateRoleDto dto) {
        return ResponseEntity.ok(service.createRole(dto));
    }

    @GetMapping
    public ResponseEntity<List<Role>> list() {
        return ResponseEntity.ok(service.listRoles());
    }

    @GetMapping("/{roleName}")
    public ResponseEntity<Role> get(@PathVariable String roleName) {
        return ResponseEntity.ok(service.getRole(roleName));
    }

    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> delete(@PathVariable String roleName) {
        service.deleteRole(roleName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleName}/attach-policy")
    public ResponseEntity<Void> attachPolicy(
            @PathVariable String roleName,
            @RequestParam String policyArn) {
        service.attachRolePolicy(roleName, policyArn);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roleName}/detach-policy")
    public ResponseEntity<Void> detachPolicy(
            @PathVariable String roleName,
            @RequestParam String policyArn) {
        service.detachRolePolicy(roleName, policyArn);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roleName}/policies")
    public ResponseEntity<List<AttachedPolicy>> listAttachedPolicies(@PathVariable String roleName) {
        return ResponseEntity.ok(service.listAttachedRolePolicies(roleName));
    }
}
