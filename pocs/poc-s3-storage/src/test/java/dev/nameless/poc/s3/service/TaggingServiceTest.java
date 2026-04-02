package dev.nameless.poc.s3.service;

import dev.nameless.poc.s3.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TaggingServiceTest extends AbstractLocalStackTest {

    @Autowired
    private TaggingService taggingService;

    @Autowired
    private BucketOperationsService bucketService;

    @Autowired
    private ObjectCrudService objectCrudService;

    private static final String BUCKET = "test-tagging-bucket";

    @BeforeEach
    void setUp() {
        if (!bucketService.bucketExists(BUCKET)) {
            bucketService.createBucket(BUCKET);
        }
    }

    @Test
    void shouldSetAndGetBucketTags() {
        taggingService.setBucketTags(BUCKET, Map.of("env", "test", "team", "platform"));

        Map<String, String> tags = taggingService.getBucketTags(BUCKET);
        assertThat(tags).containsEntry("env", "test");
        assertThat(tags).containsEntry("team", "platform");
    }

    @Test
    void shouldSetAndGetObjectTags() {
        objectCrudService.putObject(BUCKET, "tagged.txt", "data".getBytes(), "text/plain");
        taggingService.setObjectTags(BUCKET, "tagged.txt", Map.of("classification", "internal"));

        Map<String, String> tags = taggingService.getObjectTags(BUCKET, "tagged.txt");
        assertThat(tags).containsEntry("classification", "internal");
    }

    @Test
    void shouldDeleteBucketTags() {
        taggingService.setBucketTags(BUCKET, Map.of("temp", "true"));
        taggingService.deleteBucketTags(BUCKET);

        // After deletion, getBucketTags may throw or return empty
        try {
            Map<String, String> tags = taggingService.getBucketTags(BUCKET);
            assertThat(tags).isEmpty();
        } catch (Exception e) {
            // Expected — no tagging configuration exists after deletion
        }
    }
}
