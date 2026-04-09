package dev.nameless.poc.sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class SqsConfig {

    @Bean
    public SqsClient sqsClient(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {
        return SqsClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(staticCredentials(accessKey, secretKey))
                .build();
    }

    private StaticCredentialsProvider staticCredentials(String accessKey, String secretKey) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
    }
}
