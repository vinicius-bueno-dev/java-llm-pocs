package dev.nameless.poc.apigateway.service;

import dev.nameless.poc.apigateway.AbstractLocalStackTest;
import dev.nameless.poc.apigateway.dto.CreateApiDto;
import dev.nameless.poc.apigateway.dto.CreateResourceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.apigateway.model.CreateResourceResponse;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetResourcesResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.IntegrationType;
import software.amazon.awssdk.services.apigateway.model.NotFoundException;
import software.amazon.awssdk.services.apigateway.model.PutIntegrationResponse;
import software.amazon.awssdk.services.apigateway.model.PutMethodResponse;
import software.amazon.awssdk.services.apigateway.model.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestApiServiceTest extends AbstractLocalStackTest {

    @Autowired
    private RestApiService restApiService;

    @Test
    void shouldCreateAndGetRestApi() {
        CreateRestApiResponse created = restApiService.createRestApi(
                new CreateApiDto("test-api", "API de teste"));

        assertNotNull(created.id());
        assertEquals("test-api", created.name());

        GetRestApiResponse fetched = restApiService.getRestApi(created.id());
        assertEquals(created.id(), fetched.id());
        assertEquals("test-api", fetched.name());
        assertEquals("API de teste", fetched.description());
    }

    @Test
    void shouldListRestApis() {
        restApiService.createRestApi(new CreateApiDto("api-1", "Primeira API"));
        restApiService.createRestApi(new CreateApiDto("api-2", "Segunda API"));

        GetRestApisResponse response = restApiService.listRestApis();

        assertNotNull(response.items());
        assertFalse(response.items().isEmpty());
    }

    @Test
    void shouldDeleteRestApi() {
        CreateRestApiResponse created = restApiService.createRestApi(
                new CreateApiDto("api-to-delete", "Sera removida"));

        restApiService.deleteRestApi(created.id());

        assertThrows(NotFoundException.class,
                () -> restApiService.getRestApi(created.id()));
    }

    @Test
    void shouldGetResourcesForNewApi() {
        CreateRestApiResponse created = restApiService.createRestApi(
                new CreateApiDto("api-resources", "API com recursos"));

        GetResourcesResponse resources = restApiService.getResources(created.id());

        assertNotNull(resources.items());
        // Toda REST API nasce com o recurso raiz "/"
        assertFalse(resources.items().isEmpty());
        assertEquals("/", resources.items().get(0).path());
    }

    @Test
    void shouldCreateResource() {
        CreateRestApiResponse api = restApiService.createRestApi(
                new CreateApiDto("api-create-resource", "API para criar recurso"));

        GetResourcesResponse resources = restApiService.getResources(api.id());
        Resource root = resources.items().get(0);

        CreateResourceResponse resource = restApiService.createResource(
                new CreateResourceDto(api.id(), root.id(), "users"));

        assertNotNull(resource.id());
        assertEquals("users", resource.pathPart());
    }

    @Test
    void shouldPutMethodOnResource() {
        CreateRestApiResponse api = restApiService.createRestApi(
                new CreateApiDto("api-put-method", "API para metodo"));

        Resource root = restApiService.getResources(api.id()).items().get(0);

        CreateResourceResponse resource = restApiService.createResource(
                new CreateResourceDto(api.id(), root.id(), "items"));

        PutMethodResponse method = restApiService.putMethod(
                api.id(), resource.id(), "GET", "NONE");

        assertNotNull(method);
        assertEquals("GET", method.httpMethod());
    }

    @Test
    void shouldPutIntegrationOnMethod() {
        CreateRestApiResponse api = restApiService.createRestApi(
                new CreateApiDto("api-integration", "API para integracao"));

        Resource root = restApiService.getResources(api.id()).items().get(0);

        CreateResourceResponse resource = restApiService.createResource(
                new CreateResourceDto(api.id(), root.id(), "orders"));

        restApiService.putMethod(api.id(), resource.id(), "POST", "NONE");

        PutIntegrationResponse integration = restApiService.putIntegration(
                api.id(), resource.id(), "POST", IntegrationType.MOCK,
                "arn:aws:apigateway:us-east-1::/test");

        assertNotNull(integration);
        assertEquals(IntegrationType.MOCK, integration.type());
    }
}
