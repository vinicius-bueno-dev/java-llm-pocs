package dev.nameless.poc.s3.service;

import dev.nameless.poc.s3.dto.CorsRuleDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Map;

@Service
public class CorsService {

    private final S3Client s3Client;

    public CorsService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void setCorsConfiguration(String bucket, List<CorsRuleDto> rules) {
        List<CORSRule> corsRules = rules.stream()
                .map(r -> CORSRule.builder()
                        .allowedOrigins(r.allowedOrigins())
                        .allowedMethods(r.allowedMethods())
                        .allowedHeaders(r.allowedHeaders())
                        .exposeHeaders(r.exposeHeaders())
                        .maxAgeSeconds(r.maxAgeSeconds())
                        .build())
                .toList();

        s3Client.putBucketCors(PutBucketCorsRequest.builder()
                .bucket(bucket)
                .corsConfiguration(CORSConfiguration.builder()
                        .corsRules(corsRules)
                        .build())
                .build());
    }

    public List<Map<String, Object>> getCorsConfiguration(String bucket) {
        GetBucketCorsResponse response = s3Client.getBucketCors(
                GetBucketCorsRequest.builder().bucket(bucket).build());

        return response.corsRules().stream()
                .map(r -> Map.<String, Object>of(
                        "allowedOrigins", r.allowedOrigins(),
                        "allowedMethods", r.allowedMethods(),
                        "allowedHeaders", r.allowedHeaders(),
                        "exposeHeaders", r.exposeHeaders(),
                        "maxAgeSeconds", r.maxAgeSeconds()))
                .toList();
    }

    public void deleteCorsConfiguration(String bucket) {
        s3Client.deleteBucketCors(DeleteBucketCorsRequest.builder()
                .bucket(bucket)
                .build());
    }
}
