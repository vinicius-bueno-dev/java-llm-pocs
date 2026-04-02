package dev.nameless.poc.s3.service;

import dev.nameless.poc.s3.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BucketOperationsServiceTest extends AbstractLocalStackTest {

    @Autowired
    private BucketOperationsService service;

    @Test
    void shouldCreateAndListBucket() {
        String name = "test-bucket-ops-" + System.currentTimeMillis();
        service.createBucket(name);

        List<String> buckets = service.listBuckets();
        assertThat(buckets).contains(name);

        assertThat(service.bucketExists(name)).isTrue();
    }

    @Test
    void shouldDeleteBucket() {
        String name = "test-bucket-delete-" + System.currentTimeMillis();
        service.createBucket(name);
        assertThat(service.bucketExists(name)).isTrue();

        service.deleteBucket(name);
        assertThat(service.bucketExists(name)).isFalse();
    }
}
