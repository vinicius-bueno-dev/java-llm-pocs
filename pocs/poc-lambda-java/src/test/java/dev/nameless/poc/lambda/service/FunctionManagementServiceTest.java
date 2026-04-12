package dev.nameless.poc.lambda.service;

import dev.nameless.poc.lambda.AbstractLocalStackTest;
import dev.nameless.poc.lambda.dto.CreateFunctionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FunctionManagementServiceTest extends AbstractLocalStackTest {

    @Autowired
    private FunctionManagementService managementService;

    @Autowired
    private LambdaDeployService deployService;

    @Test
    void shouldCreateEchoFunction() throws IOException {
        byte[] zip = deployService.createEchoHandlerZip();
        CreateFunctionDto dto = new CreateFunctionDto(
                "test-echo-" + System.nanoTime(), "handler.handler",
                "Test echo function", 30, 256, null);

        Map<String, String> result = managementService.createFunction(dto, zip);

        assertNotNull(result.get("functionName"));
        assertNotNull(result.get("functionArn"));
    }

    @Test
    void shouldCreateFunctionWithEnvVars() throws IOException {
        byte[] zip = deployService.createEchoHandlerZip();
        Map<String, String> envVars = Map.of("ENV", "test", "APP_NAME", "poc-lambda");
        CreateFunctionDto dto = new CreateFunctionDto(
                "test-env-" + System.nanoTime(), "handler.handler",
                "Test with env vars", null, null, envVars);

        Map<String, String> result = managementService.createFunction(dto, zip);

        assertNotNull(result.get("functionArn"));

        Map<String, String> config = managementService.getFunctionConfiguration(result.get("functionName"));
        assertEquals("2", config.get("envVarCount"));
    }

    @Test
    void shouldListFunctions() throws IOException {
        byte[] zip = deployService.createEchoHandlerZip();
        managementService.createInlineFunction(
                "test-list-" + System.nanoTime(), "handler.handler", "List test", zip);

        List<Map<String, String>> functions = managementService.listFunctions();

        assertFalse(functions.isEmpty());
    }

    @Test
    void shouldGetFunctionConfiguration() throws IOException {
        String name = "test-config-" + System.nanoTime();
        byte[] zip = deployService.createEchoHandlerZip();
        managementService.createInlineFunction(name, "handler.handler", "Config test", zip);

        Map<String, String> config = managementService.getFunctionConfiguration(name);

        assertEquals(name, config.get("functionName"));
        assertEquals("handler.handler", config.get("handler"));
    }

    @Test
    void shouldUpdateFunctionConfiguration() throws IOException {
        String name = "test-update-" + System.nanoTime();
        byte[] zip = deployService.createEchoHandlerZip();
        managementService.createInlineFunction(name, "handler.handler", "Update test", zip);

        Map<String, String> result = managementService.updateFunctionConfiguration(
                name, 60, 1024, Map.of("UPDATED", "true"));

        assertEquals("60", result.get("timeout"));
        assertEquals("1024", result.get("memorySize"));
    }

    @Test
    void shouldDeleteFunction() throws IOException {
        String name = "test-delete-" + System.nanoTime();
        byte[] zip = deployService.createEchoHandlerZip();
        managementService.createInlineFunction(name, "handler.handler", "Delete test", zip);

        managementService.deleteFunction(name);

        List<Map<String, String>> functions = managementService.listFunctions();
        boolean found = functions.stream().anyMatch(f -> f.get("functionName").equals(name));
        assertFalse(found);
    }
}
