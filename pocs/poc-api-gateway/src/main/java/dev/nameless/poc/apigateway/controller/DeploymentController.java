package dev.nameless.poc.apigateway.controller;

import dev.nameless.poc.apigateway.service.DeploymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.apigateway.model.CreateDeploymentResponse;
import software.amazon.awssdk.services.apigateway.model.CreateStageResponse;
import software.amazon.awssdk.services.apigateway.model.GetStagesResponse;

@RestController
@RequestMapping("/api/gateway/deployments")
public class DeploymentController {

    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping("/{restApiId}")
    public ResponseEntity<CreateDeploymentResponse> createDeployment(
            @PathVariable String restApiId,
            @RequestParam String stageName,
            @RequestParam(required = false, defaultValue = "") String description) {
        return ResponseEntity.ok(deploymentService.createDeployment(restApiId, stageName, description));
    }

    @GetMapping("/{restApiId}/stages")
    public ResponseEntity<GetStagesResponse> getStages(@PathVariable String restApiId) {
        return ResponseEntity.ok(deploymentService.getStages(restApiId));
    }

    @PostMapping("/{restApiId}/stages")
    public ResponseEntity<CreateStageResponse> createStage(
            @PathVariable String restApiId,
            @RequestParam String stageName,
            @RequestParam String deploymentId,
            @RequestParam(required = false, defaultValue = "") String description) {
        return ResponseEntity.ok(deploymentService.createStage(restApiId, stageName,
                deploymentId, description));
    }

    @DeleteMapping("/{restApiId}/stages/{stageName}")
    public ResponseEntity<Void> deleteStage(
            @PathVariable String restApiId,
            @PathVariable String stageName) {
        deploymentService.deleteStage(restApiId, stageName);
        return ResponseEntity.noContent().build();
    }
}
