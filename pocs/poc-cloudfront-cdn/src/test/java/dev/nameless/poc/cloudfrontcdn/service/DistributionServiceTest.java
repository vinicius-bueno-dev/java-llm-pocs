package dev.nameless.poc.cloudfrontcdn.service;

import dev.nameless.poc.cloudfrontcdn.AbstractLocalStackTest;
import dev.nameless.poc.cloudfrontcdn.dto.CreateDistributionDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes de integracao para DistributionService.
 *
 * A maioria dos testes esta marcada com @Disabled porque CloudFront
 * nao e suportado no LocalStack Community Edition. Para executar
 * estes testes, utilize AWS real ou LocalStack Pro.
 *
 * O teste de setupS3Origin funciona normalmente pois depende apenas do S3.
 */
class DistributionServiceTest extends AbstractLocalStackTest {

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private OriginService originService;

    @Test
    void setupS3Origin_shouldCreateBucketAndReturnDomainName() {
        String bucketName = "test-origin-bucket";

        Map<String, String> result = originService.setupS3Origin(bucketName);

        assertNotNull(result);
        assertEquals(bucketName, result.get("bucketName"));
        assertEquals(bucketName + ".s3.amazonaws.com", result.get("domainName"));
        assertEquals("true", result.get("created"));

        // Verificar que o bucket realmente existe
        s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
    }

    @Test
    void setupS3Origin_shouldNotRecreateBucketIfExists() {
        String bucketName = "existing-origin-bucket";

        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

        Map<String, String> result = originService.setupS3Origin(bucketName);

        assertEquals("false", result.get("created"));
        assertEquals(bucketName, result.get("bucketName"));
    }

    @Test
    @Disabled("CloudFront limited in LocalStack Community — requires LocalStack Pro or real AWS")
    void createDistribution_shouldReturnDistributionWithId() {
        CreateDistributionDto dto = new CreateDistributionDto(
                "my-bucket.s3.amazonaws.com",
                "Test distribution",
                true);

        Distribution distribution = distributionService.createDistribution(dto);

        assertNotNull(distribution);
        assertNotNull(distribution.id());
        assertEquals("Test distribution", distribution.distributionConfig().comment());
    }

    @Test
    @Disabled("CloudFront limited in LocalStack Community — requires LocalStack Pro or real AWS")
    void listDistributions_shouldReturnCreatedDistributions() {
        CreateDistributionDto dto = new CreateDistributionDto(
                "list-test-bucket.s3.amazonaws.com",
                "List test",
                true);

        distributionService.createDistribution(dto);

        List<DistributionSummary> distributions = distributionService.listDistributions();

        assertNotNull(distributions);
        assertFalse(distributions.isEmpty());
    }

    @Test
    @Disabled("CloudFront limited in LocalStack Community — requires LocalStack Pro or real AWS")
    void getDistribution_shouldReturnDistributionDetails() {
        CreateDistributionDto dto = new CreateDistributionDto(
                "get-test-bucket.s3.amazonaws.com",
                "Get test",
                true);

        Distribution created = distributionService.createDistribution(dto);

        GetDistributionResponse response = distributionService.getDistribution(created.id());

        assertNotNull(response);
        assertNotNull(response.eTag());
        assertEquals(created.id(), response.distribution().id());
    }

    @Test
    @Disabled("CloudFront limited in LocalStack Community — requires LocalStack Pro or real AWS")
    void disableDistribution_shouldReturnEtag() {
        CreateDistributionDto dto = new CreateDistributionDto(
                "disable-test-bucket.s3.amazonaws.com",
                "Disable test",
                true);

        Distribution created = distributionService.createDistribution(dto);

        String etag = distributionService.disableDistribution(created.id());

        assertNotNull(etag);
        assertFalse(etag.isBlank());
    }

    @Test
    @Disabled("CloudFront limited in LocalStack Community — requires LocalStack Pro or real AWS")
    void deleteDistribution_shouldCompleteWithoutError() {
        CreateDistributionDto dto = new CreateDistributionDto(
                "delete-test-bucket.s3.amazonaws.com",
                "Delete test",
                true);

        Distribution created = distributionService.createDistribution(dto);
        String etag = distributionService.disableDistribution(created.id());

        distributionService.deleteDistribution(created.id(), etag);
    }
}
