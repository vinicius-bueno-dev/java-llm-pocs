package dev.nameless.poc.s3.service;

import dev.nameless.poc.s3.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PresignedUrlServiceTest extends AbstractLocalStackTest {

    @Autowired
    private PresignedUrlService presignedUrlService;

    @Autowired
    private ObjectCrudService objectCrudService;

    @Autowired
    private BucketOperationsService bucketService;

    private static final String BUCKET = "test-presigned-bucket";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() {
        if (!bucketService.bucketExists(BUCKET)) {
            bucketService.createBucket(BUCKET);
        }
    }

    @Test
    void shouldGeneratePresignedGetUrl() throws Exception {
        objectCrudService.putObject(BUCKET, "presigned-get.txt",
                "presigned content".getBytes(StandardCharsets.UTF_8), "text/plain");

        String url = presignedUrlService.generatePresignedGetUrl(BUCKET, "presigned-get.txt",
                Duration.ofMinutes(5));

        assertThat(url).isNotBlank();

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("presigned content");
    }

    @Test
    void shouldGeneratePresignedPutUrl() {
        String url = presignedUrlService.generatePresignedPutUrl(BUCKET, "presigned-put.txt",
                "text/plain", Duration.ofMinutes(5));

        assertThat(url).isNotBlank();
        assertThat(url).contains(BUCKET);
    }
}
