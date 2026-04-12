package dev.nameless.poc.cloudfrontcdn.service;

import dev.nameless.poc.cloudfrontcdn.dto.CreateDistributionDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.CacheBehaviors;
import software.amazon.awssdk.services.cloudfront.model.CreateDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.DefaultCacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.DeleteDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionConfig;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsRequest;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.cloudfront.model.Origins;
import software.amazon.awssdk.services.cloudfront.model.UpdateDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.ViewerProtocolPolicy;

import java.util.List;
import java.util.UUID;

/**
 * Servico para gerenciamento de distribuicoes CloudFront.
 *
 * NOTA: CloudFront tem suporte limitado no LocalStack Community Edition.
 * Operacoes como create/update/delete podem nao funcionar corretamente.
 * Este codigo foi projetado para funcionar contra AWS real.
 */
@Service
public class DistributionService {

    private final CloudFrontClient cloudFrontClient;

    public DistributionService(CloudFrontClient cloudFrontClient) {
        this.cloudFrontClient = cloudFrontClient;
    }

    /**
     * Cria uma nova distribuicao CloudFront com uma origem S3.
     */
    public Distribution createDistribution(CreateDistributionDto dto) {
        String callerReference = UUID.randomUUID().toString();
        String originId = "S3-" + dto.originDomainName();

        CreateDistributionRequest request = CreateDistributionRequest.builder()
                .distributionConfig(DistributionConfig.builder()
                        .callerReference(callerReference)
                        .comment(dto.comment())
                        .enabled(dto.enabled())
                        .origins(Origins.builder()
                                .quantity(1)
                                .items(Origin.builder()
                                        .id(originId)
                                        .domainName(dto.originDomainName())
                                        .build())
                                .build())
                        .defaultCacheBehavior(DefaultCacheBehavior.builder()
                                .targetOriginId(originId)
                                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                                .forwardedValues(fv -> fv
                                        .queryString(false)
                                        .cookies(c -> c.forward("none")))
                                .minTTL(0L)
                                .defaultTTL(86400L)
                                .maxTTL(31536000L)
                                .build())
                        .cacheBehaviors(CacheBehaviors.builder()
                                .quantity(0)
                                .build())
                        .build())
                .build();

        CreateDistributionResponse response = cloudFrontClient.createDistribution(request);
        return response.distribution();
    }

    /**
     * Lista todas as distribuicoes CloudFront.
     */
    public List<DistributionSummary> listDistributions() {
        return cloudFrontClient.listDistributions(
                ListDistributionsRequest.builder().build()
        ).distributionList().items();
    }

    /**
     * Obtem detalhes de uma distribuicao pelo ID.
     */
    public GetDistributionResponse getDistribution(String distributionId) {
        return cloudFrontClient.getDistribution(
                GetDistributionRequest.builder()
                        .id(distributionId)
                        .build());
    }

    /**
     * Desabilita uma distribuicao (passo obrigatorio antes de deletar).
     * Retorna o ETag atualizado necessario para a operacao de delete.
     */
    public String disableDistribution(String distributionId) {
        GetDistributionResponse current = getDistribution(distributionId);
        String etag = current.eTag();
        DistributionConfig config = current.distribution().distributionConfig();

        DistributionConfig disabledConfig = config.toBuilder()
                .enabled(false)
                .build();

        cloudFrontClient.updateDistribution(
                UpdateDistributionRequest.builder()
                        .id(distributionId)
                        .ifMatch(etag)
                        .distributionConfig(disabledConfig)
                        .build());

        // Retorna o ETag apos o update para uso em delete
        return getDistribution(distributionId).eTag();
    }

    /**
     * Deleta uma distribuicao. A distribuicao deve estar desabilitada e com status Deployed.
     * Em producao, e necessario aguardar a propagacao antes de deletar.
     */
    public void deleteDistribution(String distributionId, String etag) {
        cloudFrontClient.deleteDistribution(
                DeleteDistributionRequest.builder()
                        .id(distributionId)
                        .ifMatch(etag)
                        .build());
    }
}
