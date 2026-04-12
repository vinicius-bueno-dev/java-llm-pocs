package dev.nameless.poc.lambda.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class FunctionInvocationService {

    private final LambdaClient lambdaClient;

    public FunctionInvocationService(LambdaClient lambdaClient) {
        this.lambdaClient = lambdaClient;
    }

    public Map<String, Object> invokeSync(String functionName, String payload) {
        InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.REQUEST_RESPONSE)
                .payload(SdkBytes.fromUtf8String(payload != null ? payload : "{}"))
                .build());

        String responsePayload = response.payload().asString(StandardCharsets.UTF_8);
        return Map.of(
                "statusCode", response.statusCode(),
                "payload", responsePayload,
                "executedVersion", response.executedVersion() != null ? response.executedVersion() : "$LATEST",
                "functionError", response.functionError() != null ? response.functionError() : "none");
    }

    public Map<String, Object> invokeAsync(String functionName, String payload) {
        InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.EVENT)
                .payload(SdkBytes.fromUtf8String(payload != null ? payload : "{}"))
                .build());

        return Map.of(
                "statusCode", response.statusCode(),
                "accepted", response.statusCode() == 202);
    }

    public Map<String, Object> invokeDryRun(String functionName, String payload) {
        InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.DRY_RUN)
                .payload(SdkBytes.fromUtf8String(payload != null ? payload : "{}"))
                .build());

        return Map.of(
                "statusCode", response.statusCode(),
                "validated", response.statusCode() == 204);
    }
}
