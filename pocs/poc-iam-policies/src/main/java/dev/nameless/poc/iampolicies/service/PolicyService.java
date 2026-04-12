package dev.nameless.poc.iampolicies.service;

import dev.nameless.poc.iampolicies.dto.CreatePolicyDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreatePolicyRequest;
import software.amazon.awssdk.services.iam.model.CreatePolicyResponse;
import software.amazon.awssdk.services.iam.model.DeletePolicyRequest;
import software.amazon.awssdk.services.iam.model.GetPolicyRequest;
import software.amazon.awssdk.services.iam.model.GetPolicyResponse;
import software.amazon.awssdk.services.iam.model.GetPolicyVersionRequest;
import software.amazon.awssdk.services.iam.model.GetPolicyVersionResponse;
import software.amazon.awssdk.services.iam.model.ListPoliciesRequest;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.PolicyVersion;

import java.util.List;

/**
 * Gerencia operacoes de IAM Policies: criacao, listagem, consulta,
 * exclusao e versionamento de policy documents.
 */
@Service
public class PolicyService {

    private final IamClient iamClient;

    public PolicyService(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    public Policy createPolicy(CreatePolicyDto dto) {
        CreatePolicyResponse response = iamClient.createPolicy(CreatePolicyRequest.builder()
                .policyName(dto.policyName())
                .policyDocument(dto.policyDocument())
                .description(dto.description())
                .build());
        return response.policy();
    }

    public List<Policy> listPolicies() {
        return iamClient.listPolicies(ListPoliciesRequest.builder()
                .scope("Local")
                .build()).policies();
    }

    public Policy getPolicy(String policyArn) {
        GetPolicyResponse response = iamClient.getPolicy(GetPolicyRequest.builder()
                .policyArn(policyArn)
                .build());
        return response.policy();
    }

    public void deletePolicy(String policyArn) {
        iamClient.deletePolicy(DeletePolicyRequest.builder()
                .policyArn(policyArn)
                .build());
    }

    public PolicyVersion getPolicyVersion(String policyArn, String versionId) {
        GetPolicyVersionResponse response = iamClient.getPolicyVersion(GetPolicyVersionRequest.builder()
                .policyArn(policyArn)
                .versionId(versionId)
                .build());
        return response.policyVersion();
    }
}
