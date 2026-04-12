package dev.nameless.poc.lambda.service;

import dev.nameless.poc.lambda.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FunctionInvocationServiceTest extends AbstractLocalStackTest {

    @Autowired
    private FunctionManagementService managementService;

    @Autowired
    private FunctionInvocationService invocationService;

    @Autowired
    private LambdaDeployService deployService;

    private String functionName;

    @BeforeEach
    void setUp() throws IOException {
        functionName = "test-invoke-" + System.nanoTime();
        byte[] zip = deployService.createEchoHandlerZip();
        managementService.createInlineFunction(functionName, "handler.handler", "Invoke test", zip);
    }

    @Test
    void shouldInvokeSync() {
        Map<String, Object> result = invocationService.invokeSync(
                functionName, "{\"key\": \"value\"}");

        assertEquals(200, result.get("statusCode"));
        assertNotNull(result.get("payload"));
    }

    @Test
    void shouldInvokeAsync() {
        Map<String, Object> result = invocationService.invokeAsync(
                functionName, "{\"key\": \"async-value\"}");

        assertEquals(202, result.get("statusCode"));
        assertEquals(true, result.get("accepted"));
    }

    @Test
    void shouldInvokeWithEmptyPayload() {
        Map<String, Object> result = invocationService.invokeSync(functionName, null);

        assertEquals(200, result.get("statusCode"));
    }
}
