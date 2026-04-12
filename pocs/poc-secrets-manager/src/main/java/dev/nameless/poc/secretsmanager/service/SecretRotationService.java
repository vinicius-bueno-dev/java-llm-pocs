package dev.nameless.poc.secretsmanager.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SecretRotationService {

    private final SecretsManagerClient secretsManagerClient;

    public SecretRotationService(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public Map<String, String> rotateSecret(String secretId) {
        RotateSecretResponse response = secretsManagerClient.rotateSecret(
                RotateSecretRequest.builder()
                        .secretId(secretId)
                        .build());

        return Map.of(
                "arn", response.arn(),
                "name", response.name(),
                "versionId", response.versionId()
        );
    }

    public Map<String, Object> getRotationConfig(String secretId) {
        DescribeSecretResponse response = secretsManagerClient.describeSecret(
                DescribeSecretRequest.builder()
                        .secretId(secretId)
                        .build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arn", response.arn());
        result.put("name", response.name());
        result.put("rotationEnabled", response.rotationEnabled());
        result.put("rotationLambdaARN", response.rotationLambdaARN());

        if (response.rotationRules() != null) {
            Map<String, Object> rules = new LinkedHashMap<>();
            rules.put("automaticallyAfterDays", response.rotationRules().automaticallyAfterDays());
            rules.put("duration", response.rotationRules().duration());
            rules.put("scheduleExpression", response.rotationRules().scheduleExpression());
            result.put("rotationRules", rules);
        }

        result.put("lastRotatedDate",
                response.lastRotatedDate() != null ? response.lastRotatedDate().toString() : null);
        result.put("nextRotationDate",
                response.nextRotationDate() != null ? response.nextRotationDate().toString() : null);
        return result;
    }

    public List<Map<String, Object>> listSecretVersions(String secretId) {
        ListSecretVersionIdsResponse response = secretsManagerClient.listSecretVersionIds(
                ListSecretVersionIdsRequest.builder()
                        .secretId(secretId)
                        .build());

        return response.versions().stream()
                .map(version -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("versionId", version.versionId());
                    map.put("versionStages", version.versionStages());
                    map.put("createdDate",
                            version.createdDate() != null ? version.createdDate().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());
    }
}
