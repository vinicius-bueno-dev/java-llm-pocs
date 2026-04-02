package dev.nameless.poc.s3.service;

import dev.nameless.poc.s3.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VersioningServiceTest extends AbstractLocalStackTest {

    @Autowired
    private VersioningService versioningService;

    @Autowired
    private BucketOperationsService bucketService;

    private static final String BUCKET = "test-versioning-bucket";

    @BeforeEach
    void setUp() {
        if (!bucketService.bucketExists(BUCKET)) {
            bucketService.createBucket(BUCKET);
        }
        versioningService.enableVersioning(BUCKET);
    }

    @Test
    void shouldEnableAndGetVersioningStatus() {
        String status = versioningService.getVersioningStatus(BUCKET);
        assertThat(status).isEqualTo("Enabled");
    }

    @Test
    void shouldPutMultipleVersionsAndListThem() {
        String v1 = versioningService.putObjectVersioned(BUCKET, "versioned.txt",
                "version 1".getBytes(StandardCharsets.UTF_8), "text/plain");
        String v2 = versioningService.putObjectVersioned(BUCKET, "versioned.txt",
                "version 2".getBytes(StandardCharsets.UTF_8), "text/plain");

        assertThat(v1).isNotNull();
        assertThat(v2).isNotNull();
        assertThat(v1).isNotEqualTo(v2);

        List<Map<String, String>> versions = versioningService.listObjectVersions(BUCKET, "versioned.txt");
        assertThat(versions).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGetSpecificVersion() {
        String v1 = versioningService.putObjectVersioned(BUCKET, "v-get.txt",
                "first".getBytes(StandardCharsets.UTF_8), "text/plain");
        versioningService.putObjectVersioned(BUCKET, "v-get.txt",
                "second".getBytes(StandardCharsets.UTF_8), "text/plain");

        byte[] content = versioningService.getObjectVersion(BUCKET, "v-get.txt", v1);
        assertThat(new String(content, StandardCharsets.UTF_8)).isEqualTo("first");
    }
}
