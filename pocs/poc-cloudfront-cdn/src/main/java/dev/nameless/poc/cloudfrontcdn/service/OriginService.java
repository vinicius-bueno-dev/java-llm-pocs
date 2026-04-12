package dev.nameless.poc.cloudfrontcdn.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.util.List;
import java.util.Map;

/**
 * Servico para gerenciamento de origens S3 usadas por distribuicoes CloudFront.
 *
 * Fornece operacoes para configurar buckets S3 como origens e listar
 * as origens configuradas em uma distribuicao existente.
 */
@Service
public class OriginService {

    private final S3Client s3Client;
    private final CloudFrontClient cloudFrontClient;

    public OriginService(S3Client s3Client, CloudFrontClient cloudFrontClient) {
        this.s3Client = s3Client;
        this.cloudFrontClient = cloudFrontClient;
    }

    /**
     * Configura um bucket S3 como origem para CloudFront.
     * Cria o bucket se ele nao existir e retorna o domain name a ser usado
     * como {@code originDomainName} na criacao da distribuicao.
     *
     * @param bucketName nome do bucket S3
     * @return mapa com informacoes da origem (bucketName, domainName, created)
     */
    public Map<String, String> setupS3Origin(String bucketName) {
        boolean created = false;

        if (!bucketExists(bucketName)) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            created = true;
        }

        // Formato padrao do domain name S3 para uso como origem CloudFront
        String domainName = bucketName + ".s3.amazonaws.com";

        return Map.of(
                "bucketName", bucketName,
                "domainName", domainName,
                "created", String.valueOf(created));
    }

    /**
     * Lista as origens configuradas em uma distribuicao CloudFront.
     */
    public List<Origin> listOrigins(String distributionId) {
        GetDistributionResponse response = cloudFrontClient.getDistribution(
                GetDistributionRequest.builder()
                        .id(distributionId)
                        .build());

        return response.distribution()
                .distributionConfig()
                .origins()
                .items();
    }

    private boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
}
