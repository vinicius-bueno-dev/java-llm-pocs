package dev.nameless.poc.secretsmanager.service;

import dev.nameless.poc.secretsmanager.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class SecretServiceTest extends AbstractLocalStackTest {

    @Autowired
    private SecretService service;

    @Test
    void shouldCreateSecret() {
        String name = "test-secret-" + System.currentTimeMillis();
        Map<String, String> result = service.createSecret(name, "my-secret-value", null);

        assertThat(result.get("name")).isEqualTo(name);
        assertThat(result.get("secretArn")).isNotBlank();
        assertThat(result.get("versionId")).isNotBlank();
    }

    @Test
    void shouldCreateSecretWithTags() {
        String name = "test-secret-tags-" + System.currentTimeMillis();
        Map<String, String> tags = Map.of("env", "test", "team", "backend");
        Map<String, String> result = service.createSecret(name, "tagged-value", tags);

        assertThat(result.get("name")).isEqualTo(name);

        Map<String, Object> described = service.describeSecret(name);
        @SuppressWarnings("unchecked")
        Map<String, String> returnedTags = (Map<String, String>) described.get("tags");
        assertThat(returnedTags).containsEntry("env", "test");
        assertThat(returnedTags).containsEntry("team", "backend");
    }

    @Test
    void shouldGetSecretValue() {
        String name = "test-get-" + System.currentTimeMillis();
        service.createSecret(name, "secret-content", null);

        Map<String, Object> result = service.getSecretValue(name);

        assertThat(result.get("name")).isEqualTo(name);
        assertThat(result.get("secretString")).isEqualTo("secret-content");
        assertThat(result.get("versionId")).isNotNull();
        assertThat((List<?>) result.get("versionStages")).contains("AWSCURRENT");
    }

    @Test
    void shouldUpdateSecretValue() {
        String name = "test-update-" + System.currentTimeMillis();
        service.createSecret(name, "original-value", null);

        Map<String, String> updated = service.updateSecretValue(name, "updated-value");
        assertThat(updated.get("arn")).isNotBlank();
        assertThat(updated.get("versionId")).isNotBlank();

        Map<String, Object> fetched = service.getSecretValue(name);
        assertThat(fetched.get("secretString")).isEqualTo("updated-value");
    }

    @Test
    void shouldListSecrets() {
        String name = "test-list-" + System.currentTimeMillis();
        service.createSecret(name, "list-value", null);

        List<Map<String, Object>> secrets = service.listSecrets();
        assertThat(secrets).anyMatch(s -> name.equals(s.get("name")));
    }

    @Test
    void shouldDescribeSecret() {
        String name = "test-describe-" + System.currentTimeMillis();
        service.createSecret(name, "describe-value", null);

        Map<String, Object> described = service.describeSecret(name);
        assertThat(described.get("name")).isEqualTo(name);
        assertThat(described.get("arn")).isNotNull();
        assertThat(described.get("versionIdsToStages")).isNotNull();
    }

    @Test
    void shouldDeleteSecretWithForce() {
        String name = "test-delete-" + System.currentTimeMillis();
        service.createSecret(name, "delete-value", null);

        assertThatNoException().isThrownBy(() -> service.deleteSecret(name, true));
    }

    @Test
    void shouldTagSecret() {
        String name = "test-tag-" + System.currentTimeMillis();
        service.createSecret(name, "tag-value", null);

        service.tagSecret(name, Map.of("project", "nameless", "scope", "poc"));

        Map<String, Object> described = service.describeSecret(name);
        @SuppressWarnings("unchecked")
        Map<String, String> tags = (Map<String, String>) described.get("tags");
        assertThat(tags).containsEntry("project", "nameless");
        assertThat(tags).containsEntry("scope", "poc");
    }
}
