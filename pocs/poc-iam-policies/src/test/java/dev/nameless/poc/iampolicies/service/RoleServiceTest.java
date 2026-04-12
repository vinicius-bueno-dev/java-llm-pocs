package dev.nameless.poc.iampolicies.service;

import dev.nameless.poc.iampolicies.AbstractLocalStackTest;
import dev.nameless.poc.iampolicies.dto.CreatePolicyDto;
import dev.nameless.poc.iampolicies.dto.CreateRoleDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.NoSuchEntityException;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleServiceTest extends AbstractLocalStackTest {

    private static final String ASSUME_ROLE_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": {"Service": "lambda.amazonaws.com"},
                "Action": "sts:AssumeRole"
              }]
            }
            """;

    private static final String POLICY_DOCUMENT = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Action": "s3:GetObject",
                "Resource": "arn:aws:s3:::my-bucket/*"
              }]
            }
            """;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PolicyService policyService;

    @Test
    void shouldCreateAndGetRole() {
        CreateRoleDto dto = new CreateRoleDto("test-role", ASSUME_ROLE_POLICY, "A test role");
        Role created = roleService.createRole(dto);

        assertThat(created.roleName()).isEqualTo("test-role");
        assertThat(created.arn()).contains("test-role");

        Role fetched = roleService.getRole("test-role");
        assertThat(fetched.roleName()).isEqualTo("test-role");
        assertThat(fetched.description()).isEqualTo("A test role");
    }

    @Test
    void shouldListRoles() {
        roleService.createRole(new CreateRoleDto("role-a", ASSUME_ROLE_POLICY, "Role A"));
        roleService.createRole(new CreateRoleDto("role-b", ASSUME_ROLE_POLICY, "Role B"));

        List<Role> roles = roleService.listRoles();
        assertThat(roles).extracting(Role::roleName).contains("role-a", "role-b");
    }

    @Test
    void shouldDeleteRole() {
        roleService.createRole(new CreateRoleDto("to-delete", ASSUME_ROLE_POLICY, "Delete me"));
        roleService.deleteRole("to-delete");

        assertThatThrownBy(() -> roleService.getRole("to-delete"))
                .isInstanceOf(NoSuchEntityException.class);
    }

    @Test
    void shouldAttachAndDetachPolicy() {
        roleService.createRole(new CreateRoleDto("policy-role", ASSUME_ROLE_POLICY, "Role for policy"));
        Policy policy = policyService.createPolicy(
                new CreatePolicyDto("s3-read-policy", POLICY_DOCUMENT, "S3 read"));

        roleService.attachRolePolicy("policy-role", policy.arn());

        List<AttachedPolicy> attached = roleService.listAttachedRolePolicies("policy-role");
        assertThat(attached).hasSize(1);
        assertThat(attached.get(0).policyName()).isEqualTo("s3-read-policy");

        roleService.detachRolePolicy("policy-role", policy.arn());

        List<AttachedPolicy> afterDetach = roleService.listAttachedRolePolicies("policy-role");
        assertThat(afterDetach).isEmpty();
    }
}
