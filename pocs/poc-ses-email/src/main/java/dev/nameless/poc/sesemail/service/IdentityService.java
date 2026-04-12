package dev.nameless.poc.sesemail.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.DeleteIdentityRequest;
import software.amazon.awssdk.services.ses.model.GetIdentityVerificationAttributesRequest;
import software.amazon.awssdk.services.ses.model.GetIdentityVerificationAttributesResponse;
import software.amazon.awssdk.services.ses.model.IdentityVerificationAttributes;
import software.amazon.awssdk.services.ses.model.ListIdentitiesRequest;
import software.amazon.awssdk.services.ses.model.VerifyEmailIdentityRequest;

import java.util.List;
import java.util.Map;

/**
 * Gerencia identidades (email addresses) verificadas no SES.
 * No LocalStack, a verificacao e automatica — nao e necessario
 * clicar em link de confirmacao.
 */
@Service
public class IdentityService {

    private final SesClient sesClient;

    public IdentityService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    /**
     * Solicita verificacao de um endereco de email.
     * No LocalStack a identidade fica verificada imediatamente.
     */
    public void verifyEmailIdentity(String email) {
        sesClient.verifyEmailIdentity(VerifyEmailIdentityRequest.builder()
                .emailAddress(email)
                .build());
    }

    /**
     * Lista todas as identidades (emails e dominios) registradas.
     */
    public List<String> listIdentities() {
        return sesClient.listIdentities(ListIdentitiesRequest.builder().build())
                .identities();
    }

    /**
     * Retorna os atributos de verificacao para as identidades informadas.
     */
    public Map<String, IdentityVerificationAttributes> getIdentityVerificationAttributes(List<String> identities) {
        GetIdentityVerificationAttributesResponse response = sesClient.getIdentityVerificationAttributes(
                GetIdentityVerificationAttributesRequest.builder()
                        .identities(identities)
                        .build());
        return response.verificationAttributes();
    }

    /**
     * Remove uma identidade do SES.
     */
    public void deleteIdentity(String identity) {
        sesClient.deleteIdentity(DeleteIdentityRequest.builder()
                .identity(identity)
                .build());
    }
}
