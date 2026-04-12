package dev.nameless.poc.apigateway.service;

import dev.nameless.poc.apigateway.AbstractLocalStackTest;
import dev.nameless.poc.apigateway.dto.CreateApiDto;
import dev.nameless.poc.apigateway.dto.CreateResourceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.apigateway.model.CreateDeploymentResponse;
import software.amazon.awssdk.services.apigateway.model.CreateResourceResponse;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.CreateStageResponse;
import software.amazon.awssdk.services.apigateway.model.GetStagesResponse;
import software.amazon.awssdk.services.apigateway.model.IntegrationType;
import software.amazon.awssdk.services.apigateway.model.NotFoundException;
import software.amazon.awssdk.services.apigateway.model.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeploymentServiceTest extends AbstractLocalStackTest {

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private RestApiService restApiService;

    /**
     * Cria uma REST API com um recurso, metodo e integracao MOCK para
     * permitir a criacao de deployments (API Gateway exige ao menos um metodo).
     */
    private CreateRestApiResponse createApiWithMethod() {
        CreateRestApiResponse api = restApiService.createRestApi(
                new CreateApiDto("deploy-test-api", "API para testar deployments"));

        Resource root = restApiService.getResources(api.id()).items().get(0);

        CreateResourceResponse resource = restApiService.createResource(
                new CreateResourceDto(api.id(), root.id(), "health"));

        restApiService.putMethod(api.id(), resource.id(), "GET", "NONE");

        restApiService.putIntegration(api.id(), resource.id(), "GET",
                IntegrationType.MOCK, "arn:aws:apigateway:us-east-1::/test");

        return api;
    }

    @Test
    void shouldCreateDeployment() {
        CreateRestApiResponse api = createApiWithMethod();

        CreateDeploymentResponse deployment = deploymentService.createDeployment(
                api.id(), "dev", "Deploy inicial");

        assertNotNull(deployment.id());
    }

    @Test
    void shouldGetStages() {
        CreateRestApiResponse api = createApiWithMethod();

        deploymentService.createDeployment(api.id(), "dev", "Deploy dev");

        GetStagesResponse stages = deploymentService.getStages(api.id());

        assertNotNull(stages.item());
        assertFalse(stages.item().isEmpty());
    }

    @Test
    void shouldCreateStage() {
        CreateRestApiResponse api = createApiWithMethod();

        CreateDeploymentResponse deployment = deploymentService.createDeployment(
                api.id(), "dev", "Deploy base");

        CreateStageResponse stage = deploymentService.createStage(
                api.id(), "staging", deployment.id(), "Stage de staging");

        assertNotNull(stage.stageName());
        assertEquals("staging", stage.stageName());
    }

    @Test
    void shouldDeleteStage() {
        CreateRestApiResponse api = createApiWithMethod();

        CreateDeploymentResponse deployment = deploymentService.createDeployment(
                api.id(), "temp", "Deploy temporario");

        // Cria stage extra para deletar (nao pode deletar a unica stage criada pelo deployment)
        deploymentService.createStage(api.id(), "to-delete", deployment.id(), "Sera removida");

        deploymentService.deleteStage(api.id(), "to-delete");

        GetStagesResponse stages = deploymentService.getStages(api.id());
        boolean found = stages.item().stream()
                .anyMatch(s -> "to-delete".equals(s.stageName()));
        assertFalse(found, "Stage 'to-delete' nao deveria existir apos exclusao");
    }
}
