package dev.nameless.poc.cloudfrontcdn.service;

import dev.nameless.poc.cloudfrontcdn.dto.InvalidationDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.GetInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.GetInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.InvalidationSummary;
import software.amazon.awssdk.services.cloudfront.model.ListInvalidationsRequest;
import software.amazon.awssdk.services.cloudfront.model.Paths;

import java.util.List;
import java.util.UUID;

/**
 * Servico para gerenciamento de invalidacoes de cache CloudFront.
 *
 * Invalidacoes forcam o CloudFront a buscar novos objetos na origem,
 * ignorando o cache. Uteis apos atualizar conteudo no S3.
 *
 * NOTA: CloudFront tem suporte limitado no LocalStack Community Edition.
 */
@Service
public class InvalidationService {

    private final CloudFrontClient cloudFrontClient;

    public InvalidationService(CloudFrontClient cloudFrontClient) {
        this.cloudFrontClient = cloudFrontClient;
    }

    /**
     * Cria uma invalidacao para os caminhos especificados.
     */
    public CreateInvalidationResponse createInvalidation(InvalidationDto dto) {
        String callerReference = UUID.randomUUID().toString();

        CreateInvalidationRequest request = CreateInvalidationRequest.builder()
                .distributionId(dto.distributionId())
                .invalidationBatch(batch -> batch
                        .callerReference(callerReference)
                        .paths(Paths.builder()
                                .quantity(dto.paths().size())
                                .items(dto.paths())
                                .build()))
                .build();

        return cloudFrontClient.createInvalidation(request);
    }

    /**
     * Obtem o status de uma invalidacao especifica.
     */
    public GetInvalidationResponse getInvalidation(String distributionId, String invalidationId) {
        return cloudFrontClient.getInvalidation(
                GetInvalidationRequest.builder()
                        .distributionId(distributionId)
                        .id(invalidationId)
                        .build());
    }

    /**
     * Lista todas as invalidacoes de uma distribuicao.
     */
    public List<InvalidationSummary> listInvalidations(String distributionId) {
        return cloudFrontClient.listInvalidations(
                ListInvalidationsRequest.builder()
                        .distributionId(distributionId)
                        .build()
        ).invalidationList().items();
    }
}
