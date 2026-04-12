package dev.nameless.poc.iampolicies.service;

import dev.nameless.poc.iampolicies.dto.CreateRoleDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachRolePolicyRequest;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.CreateRoleRequest;
import software.amazon.awssdk.services.iam.model.CreateRoleResponse;
import software.amazon.awssdk.services.iam.model.DeleteRoleRequest;
import software.amazon.awssdk.services.iam.model.DetachRolePolicyRequest;
import software.amazon.awssdk.services.iam.model.GetRoleRequest;
import software.amazon.awssdk.services.iam.model.GetRoleResponse;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListRolesRequest;
import software.amazon.awssdk.services.iam.model.Role;

import java.util.List;

/**
 * Gerencia operacoes de IAM Roles: criacao, listagem, consulta, exclusao
 * e vinculacao/desvinculacao de policies.
 */
@Service
public class RoleService {

    private final IamClient iamClient;

    public RoleService(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    public Role createRole(CreateRoleDto dto) {
        CreateRoleResponse response = iamClient.createRole(CreateRoleRequest.builder()
                .roleName(dto.roleName())
                .assumeRolePolicyDocument(dto.assumeRolePolicyDocument())
                .description(dto.description())
                .build());
        return response.role();
    }

    public List<Role> listRoles() {
        return iamClient.listRoles(ListRolesRequest.builder().build()).roles();
    }

    public Role getRole(String roleName) {
        GetRoleResponse response = iamClient.getRole(GetRoleRequest.builder()
                .roleName(roleName)
                .build());
        return response.role();
    }

    public void deleteRole(String roleName) {
        iamClient.deleteRole(DeleteRoleRequest.builder()
                .roleName(roleName)
                .build());
    }

    public void attachRolePolicy(String roleName, String policyArn) {
        iamClient.attachRolePolicy(AttachRolePolicyRequest.builder()
                .roleName(roleName)
                .policyArn(policyArn)
                .build());
    }

    public void detachRolePolicy(String roleName, String policyArn) {
        iamClient.detachRolePolicy(DetachRolePolicyRequest.builder()
                .roleName(roleName)
                .policyArn(policyArn)
                .build());
    }

    public List<AttachedPolicy> listAttachedRolePolicies(String roleName) {
        return iamClient.listAttachedRolePolicies(ListAttachedRolePoliciesRequest.builder()
                .roleName(roleName)
                .build()).attachedPolicies();
    }
}
