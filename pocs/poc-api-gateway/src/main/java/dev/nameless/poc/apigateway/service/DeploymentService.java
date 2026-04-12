package dev.nameless.poc.apigateway.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.CreateDeploymentRequest;
import software.amazon.awssdk.services.apigateway.model.CreateDeploymentResponse;
import software.amazon.awssdk.services.apigateway.model.CreateStageRequest;
import software.amazon.awssdk.services.apigateway.model.CreateStageResponse;
import software.amazon.awssdk.services.apigateway.model.DeleteStageRequest;
import software.amazon.awssdk.services.apigateway.model.GetStagesRequest;
import software.amazon.awssdk.services.apigateway.model.GetStagesResponse;

/**
 * Servico responsavel pelo gerenciamento de deployments e stages no API Gateway.
 * Permite criar deployments, listar stages, criar e remover stages individuais.
 */
@Service
public class DeploymentService {

    private final ApiGatewayClient apiGatewayClient;

    public DeploymentService(ApiGatewayClient apiGatewayClient) {
        this.apiGatewayClient = apiGatewayClient;
    }

    public CreateDeploymentResponse createDeployment(String restApiId, String stageName,
                                                      String description) {
        return apiGatewayClient.createDeployment(CreateDeploymentRequest.builder()
                .restApiId(restApiId)
                .stageName(stageName)
                .description(description)
                .build());
    }

    public GetStagesResponse getStages(String restApiId) {
        return apiGatewayClient.getStages(GetStagesRequest.builder()
                .restApiId(restApiId)
                .build());
    }

    public CreateStageResponse createStage(String restApiId, String stageName,
                                            String deploymentId, String description) {
        return apiGatewayClient.createStage(CreateStageRequest.builder()
                .restApiId(restApiId)
                .stageName(stageName)
                .deploymentId(deploymentId)
                .description(description)
                .build());
    }

    public void deleteStage(String restApiId, String stageName) {
        apiGatewayClient.deleteStage(DeleteStageRequest.builder()
                .restApiId(restApiId)
                .stageName(stageName)
                .build());
    }
}
