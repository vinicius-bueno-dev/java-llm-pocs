package dev.nameless.poc.lambda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class LambdaConfig {

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

    @Bean
    public IamClient iamClient(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {
        return IamClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(staticCredentials(accessKey, secretKey))
                .build();
    }

    @Bean
    public CloudWatchLogsClient cloudWatchLogsClient(
            @Value("${aws.endpoint}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {
        return CloudWatchLogsClient.builder()
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
