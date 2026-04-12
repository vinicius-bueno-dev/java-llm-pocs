package dev.nameless.poc.cloudfrontcdn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configura os clientes AWS SDK v2 para CloudFront e S3.
 *
 * NOTA: CloudFront tem suporte limitado no LocalStack Community Edition.
 * Algumas operacoes podem retornar erros ou comportamento inesperado.
 * Para testes completos, utilize AWS real ou LocalStack Pro.
 */
@Configuration
public class CloudFrontConfig {

    @Bean
    public CloudFrontClient cloudFrontClient(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {
        return CloudFrontClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(staticCredentials(accessKey, secretKey))
                .build();
    }

    @Bean
    public S3Client s3Client(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(staticCredentials(accessKey, secretKey))
                .forcePathStyle(true)
                .build();
    }

    private StaticCredentialsProvider staticCredentials(String accessKey, String secretKey) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
    }
}
