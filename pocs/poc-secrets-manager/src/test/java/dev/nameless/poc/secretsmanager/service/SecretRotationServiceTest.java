package dev.nameless.poc.secretsmanager.service;

import dev.nameless.poc.secretsmanager.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecretRotationServiceTest extends AbstractLocalStackTest {

    @Autowired
    private SecretRotationService rotationService;

    @Autowired
    private SecretService secretService;

    @Test
    void shouldListSecretVersions() {
        String name = "test-versions-" + System.currentTimeMillis();
        secretService.createSecret(name, "version-1", null);

        List<Map<String, Object>> versions = rotationService.listSecretVersions(name);

        assertThat(versions).isNotEmpty();
        assertThat(versions.get(0).get("versionId")).isNotNull();
        assertThat((List<?>) versions.get(0).get("versionStages")).contains("AWSCURRENT");
    }

    @Test
    void shouldListMultipleVersionsAfterUpdate() {
        String name = "test-multi-versions-" + System.currentTimeMillis();
        secretService.createSecret(name, "value-v1", null);
        secretService.updateSecretValue(name, "value-v2");

        List<Map<String, Object>> versions = rotationService.listSecretVersions(name);

        assertThat(versions).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGetRotationConfig() {
        String name = "test-rotation-config-" + System.currentTimeMillis();
        secretService.createSecret(name, "rotation-value", null);

        Map<String, Object> config = rotationService.getRotationConfig(name);

        assertThat(config.get("name")).isEqualTo(name);
        assertThat(config.get("arn")).isNotNull();
    }
}
