package dev.nameless.poc.iampolicies.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.EvaluationResult;
import software.amazon.awssdk.services.iam.model.SimulatePrincipalPolicyRequest;
import software.amazon.awssdk.services.iam.model.SimulatePrincipalPolicyResponse;

import java.util.List;

/**
 * Utiliza o IAM Policy Simulator para avaliar se um principal (role/user)
 * tem permissao para executar determinadas acoes em determinados recursos.
 */
@Service
public class PolicySimulatorService {

    private final IamClient iamClient;

    public PolicySimulatorService(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    public List<EvaluationResult> simulatePrincipalPolicy(
            String policySourceArn,
            List<String> actionNames,
            List<String> resourceArns) {
        SimulatePrincipalPolicyResponse response = iamClient.simulatePrincipalPolicy(
                SimulatePrincipalPolicyRequest.builder()
                        .policySourceArn(policySourceArn)
                        .actionNames(actionNames)
                        .resourceArns(resourceArns)
                        .build());
        return response.evaluationResults();
    }
}
