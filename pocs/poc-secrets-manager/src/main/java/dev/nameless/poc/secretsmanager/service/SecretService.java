package dev.nameless.poc.secretsmanager.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SecretService {

    private final SecretsManagerClient secretsManagerClient;

    public SecretService(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public Map<String, String> createSecret(String name, String value, Map<String, String> tags) {
        CreateSecretRequest.Builder builder = CreateSecretRequest.builder()
                .name(name)
                .secretString(value);

        if (tags != null && !tags.isEmpty()) {
            List<Tag> awsTags = tags.entrySet().stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .toList();
            builder.tags(awsTags);
        }

        CreateSecretResponse response = secretsManagerClient.createSecret(builder.build());

        return Map.of(
                "secretArn", response.arn(),
                "name", response.name(),
                "versionId", response.versionId()
        );
    }

    public Map<String, Object> getSecretValue(String secretId) {
        GetSecretValueResponse response = secretsManagerClient.getSecretValue(
                GetSecretValueRequest.builder()
                        .secretId(secretId)
                        .build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", response.name());
        result.put("secretString", response.secretString());
        result.put("versionId", response.versionId());
        result.put("versionStages", response.versionStages());
        return result;
    }

    public Map<String, Object> getSecretValueByStage(String secretId, String versionStage) {
        GetSecretValueResponse response = secretsManagerClient.getSecretValue(
                GetSecretValueRequest.builder()
                        .secretId(secretId)
                        .versionStage(versionStage)
                        .build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", response.name());
        result.put("secretString", response.secretString());
        result.put("versionId", response.versionId());
        result.put("versionStages", response.versionStages());
        return result;
    }

    public List<Map<String, Object>> listSecrets() {
        ListSecretsResponse response = secretsManagerClient.listSecrets(
                ListSecretsRequest.builder().build());

        return response.secretList().stream()
                .map(secret -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", secret.name());
                    map.put("arn", secret.arn());
                    map.put("lastChangedDate",
                            secret.lastChangedDate() != null ? secret.lastChangedDate().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> describeSecret(String secretId) {
        DescribeSecretResponse response = secretsManagerClient.describeSecret(
                DescribeSecretRequest.builder()
                        .secretId(secretId)
                        .build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arn", response.arn());
        result.put("name", response.name());
        result.put("description", response.description());
        result.put("lastChangedDate",
                response.lastChangedDate() != null ? response.lastChangedDate().toString() : null);
        result.put("lastAccessedDate",
                response.lastAccessedDate() != null ? response.lastAccessedDate().toString() : null);
        result.put("deletedDate",
                response.deletedDate() != null ? response.deletedDate().toString() : null);
        result.put("tags", response.tags().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value)));
        result.put("versionIdsToStages", response.versionIdsToStages());
        return result;
    }

    public Map<String, String> updateSecretValue(String secretId, String newValue) {
        UpdateSecretResponse response = secretsManagerClient.updateSecret(
                UpdateSecretRequest.builder()
                        .secretId(secretId)
                        .secretString(newValue)
                        .build());

        return Map.of(
                "arn", response.arn(),
                "versionId", response.versionId()
        );
    }

    public Map<String, Object> putSecretValue(String secretId, String value, List<String> versionStages) {
        PutSecretValueRequest.Builder builder = PutSecretValueRequest.builder()
                .secretId(secretId)
                .secretString(value);

        if (versionStages != null && !versionStages.isEmpty()) {
            builder.versionStages(versionStages);
        }

        PutSecretValueResponse response = secretsManagerClient.putSecretValue(builder.build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("arn", response.arn());
        result.put("name", response.name());
        result.put("versionId", response.versionId());
        result.put("versionStages", response.versionStages());
        return result;
    }

    public void deleteSecret(String secretId, boolean forceDelete) {
        DeleteSecretRequest.Builder builder = DeleteSecretRequest.builder()
                .secretId(secretId);

        if (forceDelete) {
            builder.forceDeleteWithoutRecovery(true);
        }

        secretsManagerClient.deleteSecret(builder.build());
    }

    public Map<String, String> restoreSecret(String secretId) {
        RestoreSecretResponse response = secretsManagerClient.restoreSecret(
                RestoreSecretRequest.builder()
                        .secretId(secretId)
                        .build());

        return Map.of(
                "name", response.name(),
                "arn", response.arn()
        );
    }

    public void tagSecret(String secretId, Map<String, String> tags) {
        List<Tag> awsTags = tags.entrySet().stream()
                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                .toList();

        secretsManagerClient.tagResource(TagResourceRequest.builder()
                .secretId(secretId)
                .tags(awsTags)
                .build());
    }
}
