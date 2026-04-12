package dev.nameless.poc.cloudfrontcdn.dto;

import java.util.List;

/**
 * DTO para criacao de uma invalidacao CloudFront.
 *
 * @param distributionId ID da distribuicao alvo
 * @param paths          lista de caminhos a invalidar (ex.: "/images/*", "/index.html")
 */
public record InvalidationDto(
        String distributionId,
        List<String> paths
) {
}
