package dev.nameless.poc.iampolicies.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

/**
 * Demonstra operacoes STS: AssumeRole para obter credenciais temporarias
 * e GetCallerIdentity para verificar a identidade atual.
 */
@Service
public class AssumeRoleService {

    private final StsClient stsClient;

    public AssumeRoleService(StsClient stsClient) {
        this.stsClient = stsClient;
    }

    public Credentials assumeRole(String roleArn, String sessionName) {
        AssumeRoleResponse response = stsClient.assumeRole(AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName(sessionName)
                .durationSeconds(3600)
                .build());
        return response.credentials();
    }

    public GetCallerIdentityResponse getCallerIdentity() {
        return stsClient.getCallerIdentity(GetCallerIdentityRequest.builder().build());
    }
}
