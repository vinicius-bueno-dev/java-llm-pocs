package dev.nameless.poc.kmsencryption.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.AliasListEntry;
import software.amazon.awssdk.services.kms.model.CreateAliasRequest;
import software.amazon.awssdk.services.kms.model.CreateKeyRequest;
import software.amazon.awssdk.services.kms.model.CreateKeyResponse;
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.DisableKeyRequest;
import software.amazon.awssdk.services.kms.model.EnableKeyRotationRequest;
import software.amazon.awssdk.services.kms.model.GetKeyRotationStatusRequest;
import software.amazon.awssdk.services.kms.model.GetKeyRotationStatusResponse;
import software.amazon.awssdk.services.kms.model.KeyListEntry;
import software.amazon.awssdk.services.kms.model.KeyMetadata;
import software.amazon.awssdk.services.kms.model.ListAliasesRequest;
import software.amazon.awssdk.services.kms.model.ListAliasesResponse;
import software.amazon.awssdk.services.kms.model.ListKeysRequest;
import software.amazon.awssdk.services.kms.model.ListKeysResponse;
import software.amazon.awssdk.services.kms.model.ScheduleKeyDeletionRequest;
import software.amazon.awssdk.services.kms.model.ScheduleKeyDeletionResponse;
import software.amazon.awssdk.services.kms.model.Tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeyManagementService {

    private final KmsClient kmsClient;

    public KeyManagementService(KmsClient kmsClient) {
        this.kmsClient = kmsClient;
    }

    public Map<String, String> createKey(String description, Map<String, String> tags) {
        CreateKeyRequest.Builder builder = CreateKeyRequest.builder()
                .description(description);

        if (tags != null && !tags.isEmpty()) {
            List<Tag> kmsTags = tags.entrySet().stream()
                    .map(e -> Tag.builder().tagKey(e.getKey()).tagValue(e.getValue()).build())
                    .toList();
            builder.tags(kmsTags);
        }

        CreateKeyResponse response = kmsClient.createKey(builder.build());
        KeyMetadata metadata = response.keyMetadata();

        Map<String, String> result = new LinkedHashMap<>();
        result.put("keyId", metadata.keyId());
        result.put("keyArn", metadata.arn());
        return result;
    }

    public Map<String, String> createKeyWithAlias(String description, String alias) {
        Map<String, String> keyResult = createKey(description, null);
        String keyId = keyResult.get("keyId");

        String aliasName = alias.startsWith("alias/") ? alias : "alias/" + alias;
        createAlias(aliasName, keyId);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("keyId", keyId);
        result.put("alias", aliasName);
        return result;
    }

    public List<Map<String, String>> listKeys() {
        ListKeysResponse response = kmsClient.listKeys(ListKeysRequest.builder().build());
        return response.keys().stream()
                .map(this::toKeyMap)
                .toList();
    }

    public Map<String, String> describeKey(String keyId) {
        DescribeKeyResponse response = kmsClient.describeKey(
                DescribeKeyRequest.builder().keyId(keyId).build());
        KeyMetadata metadata = response.keyMetadata();

        Map<String, String> result = new LinkedHashMap<>();
        result.put("keyId", metadata.keyId());
        result.put("arn", metadata.arn());
        result.put("state", metadata.keyStateAsString());
        result.put("description", metadata.description());
        result.put("creationDate", metadata.creationDate().toString());
        return result;
    }

    public void enableKeyRotation(String keyId) {
        kmsClient.enableKeyRotation(
                EnableKeyRotationRequest.builder().keyId(keyId).build());
    }

    public Map<String, Object> getKeyRotationStatus(String keyId) {
        GetKeyRotationStatusResponse response = kmsClient.getKeyRotationStatus(
                GetKeyRotationStatusRequest.builder().keyId(keyId).build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", response.keyRotationEnabled());
        return result;
    }

    public void createAlias(String aliasName, String keyId) {
        kmsClient.createAlias(CreateAliasRequest.builder()
                .aliasName(aliasName)
                .targetKeyId(keyId)
                .build());
    }

    public List<Map<String, String>> listAliases() {
        ListAliasesResponse response = kmsClient.listAliases(
                ListAliasesRequest.builder().build());
        return response.aliases().stream()
                .map(this::toAliasMap)
                .toList();
    }

    public void disableKey(String keyId) {
        kmsClient.disableKey(DisableKeyRequest.builder().keyId(keyId).build());
    }

    public Map<String, String> scheduleKeyDeletion(String keyId, int pendingWindowDays) {
        ScheduleKeyDeletionResponse response = kmsClient.scheduleKeyDeletion(
                ScheduleKeyDeletionRequest.builder()
                        .keyId(keyId)
                        .pendingWindowInDays(pendingWindowDays)
                        .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("keyId", response.keyId());
        result.put("deletionDate", response.deletionDate().toString());
        return result;
    }

    private Map<String, String> toKeyMap(KeyListEntry entry) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("keyId", entry.keyId());
        map.put("keyArn", entry.keyArn());
        return map;
    }

    private Map<String, String> toAliasMap(AliasListEntry entry) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("aliasName", entry.aliasName());
        map.put("aliasArn", entry.aliasArn());
        map.put("targetKeyId", entry.targetKeyId());
        return map;
    }
}
