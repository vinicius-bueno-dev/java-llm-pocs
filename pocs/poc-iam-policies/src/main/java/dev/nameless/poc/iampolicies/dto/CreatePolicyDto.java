package dev.nameless.poc.iampolicies.dto;

public record CreatePolicyDto(
        String policyName,
        String policyDocument,
        String description) {
}
