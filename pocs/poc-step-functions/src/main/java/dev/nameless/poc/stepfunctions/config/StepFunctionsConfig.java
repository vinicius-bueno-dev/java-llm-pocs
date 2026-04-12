package dev.nameless.poc.stepfunctions.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.net.URI;

@Configuration
public class StepFunctionsConfig {

    @Bean
    public SfnClient sfnClient(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {
        return SfnClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(staticCredentials(accessKey, secretKey))
                .build();
    }

    @Bean
    public LambdaClient lambdaClient(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {
        return LambdaClient.builder()
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
