package dev.nameless.poc.apigateway.service;

import dev.nameless.poc.apigateway.dto.CreateApiDto;
import dev.nameless.poc.apigateway.dto.CreateResourceDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.CreateResourceRequest;
import software.amazon.awssdk.services.apigateway.model.CreateResourceResponse;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.DeleteRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.GetResourcesRequest;
import software.amazon.awssdk.services.apigateway.model.GetResourcesResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.GetRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.IntegrationType;
import software.amazon.awssdk.services.apigateway.model.PutIntegrationRequest;
import software.amazon.awssdk.services.apigateway.model.PutIntegrationResponse;
import software.amazon.awssdk.services.apigateway.model.PutMethodRequest;
import software.amazon.awssdk.services.apigateway.model.PutMethodResponse;

/**
 * Servico responsavel pelo gerenciamento de REST APIs no API Gateway.
 * Encapsula operacoes de CRUD de APIs, recursos, metodos e integracoes.
 */
@Service
public class RestApiService {

    private final ApiGatewayClient apiGatewayClient;

    public RestApiService(ApiGatewayClient apiGatewayClient) {
        this.apiGatewayClient = apiGatewayClient;
    }

    public CreateRestApiResponse createRestApi(CreateApiDto dto) {
        return apiGatewayClient.createRestApi(CreateRestApiRequest.builder()
                .name(dto.name())
                .description(dto.description())
                .build());
    }

    public GetRestApisResponse listRestApis() {
        return apiGatewayClient.getRestApis();
    }

    public GetRestApiResponse getRestApi(String restApiId) {
        return apiGatewayClient.getRestApi(GetRestApiRequest.builder()
                .restApiId(restApiId)
                .build());
    }

    public void deleteRestApi(String restApiId) {
        apiGatewayClient.deleteRestApi(DeleteRestApiRequest.builder()
                .restApiId(restApiId)
                .build());
    }

    public GetResourcesResponse getResources(String restApiId) {
        return apiGatewayClient.getResources(GetResourcesRequest.builder()
                .restApiId(restApiId)
                .build());
    }

    public CreateResourceResponse createResource(CreateResourceDto dto) {
        return apiGatewayClient.createResource(CreateResourceRequest.builder()
                .restApiId(dto.restApiId())
                .parentId(dto.parentId())
                .pathPart(dto.pathPart())
                .build());
    }

    public PutMethodResponse putMethod(String restApiId, String resourceId,
                                       String httpMethod, String authorizationType) {
        return apiGatewayClient.putMethod(PutMethodRequest.builder()
                .restApiId(restApiId)
                .resourceId(resourceId)
                .httpMethod(httpMethod)
                .authorizationType(authorizationType)
                .build());
    }

    public PutIntegrationResponse putIntegration(String restApiId, String resourceId,
                                                  String httpMethod, IntegrationType type,
                                                  String uri) {
        return apiGatewayClient.putIntegration(PutIntegrationRequest.builder()
                .restApiId(restApiId)
                .resourceId(resourceId)
                .httpMethod(httpMethod)
                .type(type)
                .integrationHttpMethod("POST")
                .uri(uri)
                .build());
    }
}
