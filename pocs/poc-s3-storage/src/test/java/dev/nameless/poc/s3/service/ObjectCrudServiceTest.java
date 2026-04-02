package dev.nameless.poc.s3.service;

import dev.nameless.poc.s3.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectCrudServiceTest extends AbstractLocalStackTest {

    @Autowired
    private ObjectCrudService objectCrudService;

    @Autowired
    private BucketOperationsService bucketService;

    private static final String BUCKET = "test-crud-bucket";

    @BeforeEach
    void setUp() {
        if (!bucketService.bucketExists(BUCKET)) {
            bucketService.createBucket(BUCKET);
        }
    }

    @Test
    void shouldPutAndGetObject() {
        byte[] content = "Hello S3!".getBytes(StandardCharsets.UTF_8);
        objectCrudService.putObject(BUCKET, "test.txt", content, "text/plain");

        byte[] retrieved = objectCrudService.getObject(BUCKET, "test.txt");
        assertThat(new String(retrieved, StandardCharsets.UTF_8)).isEqualTo("Hello S3!");
    }

    @Test
    void shouldListObjects() {
        objectCrudService.putObject(BUCKET, "dir/a.txt", "a".getBytes(), "text/plain");
        objectCrudService.putObject(BUCKET, "dir/b.txt", "b".getBytes(), "text/plain");

        List<String> keys = objectCrudService.listObjects(BUCKET, "dir/", 100);
        assertThat(keys).contains("dir/a.txt", "dir/b.txt");
    }

    @Test
    void shouldHeadObject() {
        objectCrudService.putObject(BUCKET, "meta.txt", "data".getBytes(), "text/plain");

        Map<String, String> metadata = objectCrudService.headObject(BUCKET, "meta.txt");
        assertThat(metadata).containsKey("contentType");
        assertThat(metadata).containsKey("eTag");
    }

    @Test
    void shouldDeleteObject() {
        objectCrudService.putObject(BUCKET, "to-delete.txt", "bye".getBytes(), "text/plain");
        objectCrudService.deleteObject(BUCKET, "to-delete.txt");

        List<String> keys = objectCrudService.listObjects(BUCKET, "to-delete", 100);
        assertThat(keys).doesNotContain("to-delete.txt");
    }

    @Test
    void shouldBatchDeleteObjects() {
        objectCrudService.putObject(BUCKET, "batch/1.txt", "1".getBytes(), "text/plain");
        objectCrudService.putObject(BUCKET, "batch/2.txt", "2".getBytes(), "text/plain");

        int deleted = objectCrudService.deleteObjects(BUCKET, List.of("batch/1.txt", "batch/2.txt"));
        assertThat(deleted).isEqualTo(2);
    }

    @Test
    void shouldCopyObject() {
        objectCrudService.putObject(BUCKET, "original.txt", "copy me".getBytes(), "text/plain");
        objectCrudService.copyObject(BUCKET, "original.txt", BUCKET, "copied.txt");

        byte[] copied = objectCrudService.getObject(BUCKET, "copied.txt");
        assertThat(new String(copied, StandardCharsets.UTF_8)).isEqualTo("copy me");
    }
}
