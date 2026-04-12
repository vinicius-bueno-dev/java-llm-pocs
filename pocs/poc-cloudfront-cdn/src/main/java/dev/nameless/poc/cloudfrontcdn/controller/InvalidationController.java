package dev.nameless.poc.cloudfrontcdn.controller;

import dev.nameless.poc.cloudfrontcdn.dto.InvalidationDto;
import dev.nameless.poc.cloudfrontcdn.service.InvalidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.GetInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.InvalidationSummary;

import java.util.List;

@RestController
@RequestMapping("/api/cloudfront/invalidations")
public class InvalidationController {

    private final InvalidationService invalidationService;

    public InvalidationController(InvalidationService invalidationService) {
        this.invalidationService = invalidationService;
    }

    @PostMapping
    public ResponseEntity<CreateInvalidationResponse> create(@RequestBody InvalidationDto dto) {
        return ResponseEntity.ok(invalidationService.createInvalidation(dto));
    }

    @GetMapping("/{distributionId}/{invalidationId}")
    public ResponseEntity<GetInvalidationResponse> get(
            @PathVariable String distributionId,
            @PathVariable String invalidationId) {
        return ResponseEntity.ok(invalidationService.getInvalidation(distributionId, invalidationId));
    }

    @GetMapping("/{distributionId}")
    public ResponseEntity<List<InvalidationSummary>> list(@PathVariable String distributionId) {
        return ResponseEntity.ok(invalidationService.listInvalidations(distributionId));
    }
}
