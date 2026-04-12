package dev.nameless.poc.cloudfrontcdn.dto;

/**
 * DTO para criacao de uma distribuicao CloudFront.
 *
 * @param originDomainName dominio de origem (ex.: bucket-name.s3.amazonaws.com)
 * @param comment          descricao da distribuicao
 * @param enabled          se a distribuicao deve ser criada habilitada
 */
public record CreateDistributionDto(
        String originDomainName,
        String comment,
        boolean enabled
) {
}
