package dev.nameless.poc.kmsencryption.service;

import dev.nameless.poc.kmsencryption.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeyManagementServiceTest extends AbstractLocalStackTest {

    @Autowired
    private KeyManagementService service;

    @Test
    void shouldCreateKeyAndDescribe() {
        Map<String, String> created = service.createKey("test-key", Map.of("env", "test"));

        assertThat(created).containsKeys("keyId", "keyArn");
        assertThat(created.get("keyId")).isNotBlank();

        Map<String, String> described = service.describeKey(created.get("keyId"));
        assertThat(described.get("keyId")).isEqualTo(created.get("keyId"));
        assertThat(described.get("description")).isEqualTo("test-key");
        assertThat(described.get("state")).isEqualTo("Enabled");
        assertThat(described.get("creationDate")).isNotBlank();
    }

    @Test
    void shouldCreateKeyWithAlias() {
        Map<String, String> result = service.createKeyWithAlias("aliased-key", "my-test-alias");

        assertThat(result).containsKeys("keyId", "alias");
        assertThat(result.get("alias")).isEqualTo("alias/my-test-alias");
    }

    @Test
    void shouldListKeys() {
        service.createKey("key-1", null);
        service.createKey("key-2", null);

        List<Map<String, String>> keys = service.listKeys();
        assertThat(keys).hasSizeGreaterThanOrEqualTo(2);
        assertThat(keys.get(0)).containsKeys("keyId", "keyArn");
    }

    @Test
    void shouldCreateAndListAliases() {
        Map<String, String> key = service.createKey("alias-test-key", null);
        service.createAlias("alias/test-list-alias", key.get("keyId"));

        List<Map<String, String>> aliases = service.listAliases();
        assertThat(aliases).anySatisfy(alias -> {
            assertThat(alias.get("aliasName")).isEqualTo("alias/test-list-alias");
            assertThat(alias.get("targetKeyId")).isEqualTo(key.get("keyId"));
        });
    }

    @Test
    void shouldEnableAndCheckKeyRotation() {
        Map<String, String> key = service.createKey("rotation-key", null);
        String keyId = key.get("keyId");

        service.enableKeyRotation(keyId);
        Map<String, Object> status = service.getKeyRotationStatus(keyId);

        assertThat(status.get("enabled")).isEqualTo(true);
    }

    @Test
    void shouldDisableKey() {
        Map<String, String> key = service.createKey("disable-key", null);
        String keyId = key.get("keyId");

        service.disableKey(keyId);

        Map<String, String> described = service.describeKey(keyId);
        assertThat(described.get("state")).isEqualTo("Disabled");
    }

    @Test
    void shouldScheduleKeyDeletion() {
        Map<String, String> key = service.createKey("delete-key", null);
        String keyId = key.get("keyId");

        Map<String, String> result = service.scheduleKeyDeletion(keyId, 7);
        assertThat(result).containsKeys("keyId", "deletionDate");
        assertThat(result.get("keyId")).isEqualTo(keyId);
        assertThat(result.get("deletionDate")).isNotBlank();
    }
}
