package dev.nameless.poc.cloudfrontcdn.controller;

import dev.nameless.poc.cloudfrontcdn.dto.CreateDistributionDto;
import dev.nameless.poc.cloudfrontcdn.service.DistributionService;
import dev.nameless.poc.cloudfrontcdn.service.OriginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.Origin;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cloudfront/distributions")
public class DistributionController {

    private final DistributionService distributionService;
    private final OriginService originService;

    public DistributionController(DistributionService distributionService, OriginService originService) {
        this.distributionService = distributionService;
        this.originService = originService;
    }

    @PostMapping
    public ResponseEntity<Distribution> create(@RequestBody CreateDistributionDto dto) {
        Distribution distribution = distributionService.createDistribution(dto);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping
    public ResponseEntity<List<DistributionSummary>> list() {
        return ResponseEntity.ok(distributionService.listDistributions());
    }

    @GetMapping("/{distributionId}")
    public ResponseEntity<GetDistributionResponse> get(@PathVariable String distributionId) {
        return ResponseEntity.ok(distributionService.getDistribution(distributionId));
    }

    @PostMapping("/{distributionId}/disable")
    public ResponseEntity<Map<String, String>> disable(@PathVariable String distributionId) {
        String etag = distributionService.disableDistribution(distributionId);
        return ResponseEntity.ok(Map.of(
                "distributionId", distributionId,
                "etag", etag,
                "status", "disabled"));
    }

    @DeleteMapping("/{distributionId}")
    public ResponseEntity<Void> delete(
            @PathVariable String distributionId,
            @RequestParam String etag) {
        distributionService.deleteDistribution(distributionId, etag);
        return ResponseEntity.noContent().build();
    }

    /**
     * Configura um bucket S3 como origem para uso posterior em distribuicoes.
     */
    @PostMapping("/origins/s3")
    public ResponseEntity<Map<String, String>> setupS3Origin(@RequestParam String bucketName) {
        return ResponseEntity.ok(originService.setupS3Origin(bucketName));
    }

    /**
     * Lista as origens de uma distribuicao existente.
     */
    @GetMapping("/{distributionId}/origins")
    public ResponseEntity<List<Origin>> listOrigins(@PathVariable String distributionId) {
        return ResponseEntity.ok(originService.listOrigins(distributionId));
    }
}
