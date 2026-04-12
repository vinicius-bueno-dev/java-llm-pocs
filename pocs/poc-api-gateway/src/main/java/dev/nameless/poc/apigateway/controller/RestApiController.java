package dev.nameless.poc.apigateway.controller;

import dev.nameless.poc.apigateway.dto.CreateApiDto;
import dev.nameless.poc.apigateway.dto.CreateResourceDto;
import dev.nameless.poc.apigateway.service.RestApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.apigateway.model.CreateResourceResponse;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetResourcesResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.IntegrationType;
import software.amazon.awssdk.services.apigateway.model.PutIntegrationResponse;
import software.amazon.awssdk.services.apigateway.model.PutMethodResponse;

@RestController
@RequestMapping("/api/gateway/apis")
public class RestApiController {

    private final RestApiService restApiService;

    public RestApiController(RestApiService restApiService) {
        this.restApiService = restApiService;
    }

    @PostMapping
    public ResponseEntity<CreateRestApiResponse> createRestApi(@RequestBody CreateApiDto dto) {
        return ResponseEntity.ok(restApiService.createRestApi(dto));
    }

    @GetMapping
    public ResponseEntity<GetRestApisResponse> listRestApis() {
        return ResponseEntity.ok(restApiService.listRestApis());
    }

    @GetMapping("/{restApiId}")
    public ResponseEntity<GetRestApiResponse> getRestApi(@PathVariable String restApiId) {
        return ResponseEntity.ok(restApiService.getRestApi(restApiId));
    }

    @DeleteMapping("/{restApiId}")
    public ResponseEntity<Void> deleteRestApi(@PathVariable String restApiId) {
        restApiService.deleteRestApi(restApiId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{restApiId}/resources")
    public ResponseEntity<GetResourcesResponse> getResources(@PathVariable String restApiId) {
        return ResponseEntity.ok(restApiService.getResources(restApiId));
    }

    @PostMapping("/resources")
    public ResponseEntity<CreateResourceResponse> createResource(@RequestBody CreateResourceDto dto) {
        return ResponseEntity.ok(restApiService.createResource(dto));
    }

    @PutMapping("/{restApiId}/resources/{resourceId}/methods/{httpMethod}")
    public ResponseEntity<PutMethodResponse> putMethod(
            @PathVariable String restApiId,
            @PathVariable String resourceId,
            @PathVariable String httpMethod,
            @RequestParam(defaultValue = "NONE") String authorizationType) {
        return ResponseEntity.ok(restApiService.putMethod(restApiId, resourceId,
                httpMethod, authorizationType));
    }

    @PutMapping("/{restApiId}/resources/{resourceId}/methods/{httpMethod}/integration")
    public ResponseEntity<PutIntegrationResponse> putIntegration(
            @PathVariable String restApiId,
            @PathVariable String resourceId,
            @PathVariable String httpMethod,
            @RequestParam String type,
            @RequestParam String uri) {
        return ResponseEntity.ok(restApiService.putIntegration(restApiId, resourceId,
                httpMethod, IntegrationType.fromValue(type), uri));
    }
}
