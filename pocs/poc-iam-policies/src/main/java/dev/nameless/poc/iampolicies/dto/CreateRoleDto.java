package dev.nameless.poc.iampolicies.dto;

public record CreateRoleDto(
        String roleName,
        String assumeRolePolicyDocument,
        String description) {
}
