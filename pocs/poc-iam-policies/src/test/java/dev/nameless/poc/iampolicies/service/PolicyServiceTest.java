package dev.nameless.poc.iampolicies.service;

import dev.nameless.poc.iampolicies.AbstractLocalStackTest;
import dev.nameless.poc.iampolicies.dto.CreatePolicyDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.iam.model.NoSuchEntityException;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.PolicyVersion;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyServiceTest extends AbstractLocalStackTest {

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
    private PolicyService policyService;

    @Test
    void shouldCreateAndGetPolicy() {
        CreatePolicyDto dto = new CreatePolicyDto("test-policy", POLICY_DOCUMENT, "A test policy");
        Policy created = policyService.createPolicy(dto);

        assertThat(created.policyName()).isEqualTo("test-policy");
        assertThat(created.arn()).contains("test-policy");

        Policy fetched = policyService.getPolicy(created.arn());
        assertThat(fetched.policyName()).isEqualTo("test-policy");
        assertThat(fetched.description()).isEqualTo("A test policy");
    }

    @Test
    void shouldListLocalPolicies() {
        policyService.createPolicy(new CreatePolicyDto("policy-x", POLICY_DOCUMENT, "X"));
        policyService.createPolicy(new CreatePolicyDto("policy-y", POLICY_DOCUMENT, "Y"));

        List<Policy> policies = policyService.listPolicies();
        assertThat(policies).extracting(Policy::policyName).contains("policy-x", "policy-y");
    }

    @Test
    void shouldDeletePolicy() {
        Policy created = policyService.createPolicy(
                new CreatePolicyDto("to-delete-policy", POLICY_DOCUMENT, "Delete me"));

        policyService.deletePolicy(created.arn());

        assertThatThrownBy(() -> policyService.getPolicy(created.arn()))
                .isInstanceOf(NoSuchEntityException.class);
    }

    @Test
    void shouldGetPolicyVersion() {
        Policy created = policyService.createPolicy(
                new CreatePolicyDto("versioned-policy", POLICY_DOCUMENT, "Versioned"));

        PolicyVersion version = policyService.getPolicyVersion(created.arn(), "v1");
        assertThat(version.isDefaultVersion()).isTrue();
        assertThat(version.document()).isNotBlank();
    }
}
