package dev.nameless.poc.lambda.service;

import dev.nameless.poc.lambda.dto.CreateFunctionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FunctionManagementService {

    private final LambdaClient lambdaClient;
    private final String roleArn;
    private final String defaultRuntime;
    private final int defaultTimeout;
    private final int defaultMemorySize;

    public FunctionManagementService(
            LambdaClient lambdaClient,
            @Value("${aws.lambda.role-arn}") String roleArn,
            @Value("${aws.lambda.runtime}") String defaultRuntime,
            @Value("${aws.lambda.timeout}") int defaultTimeout,
            @Value("${aws.lambda.memory-size}") int defaultMemorySize) {
        this.lambdaClient = lambdaClient;
        this.roleArn = roleArn;
        this.defaultRuntime = defaultRuntime;
        this.defaultTimeout = defaultTimeout;
        this.defaultMemorySize = defaultMemorySize;
    }

    public Map<String, String> createFunction(CreateFunctionDto dto, byte[] zipBytes) {
        FunctionCode code = FunctionCode.builder()
                .zipFile(SdkBytes.fromByteArray(zipBytes))
                .build();

        CreateFunctionRequest.Builder builder = CreateFunctionRequest.builder()
                .functionName(dto.functionName())
                .runtime(defaultRuntime)
                .role(roleArn)
                .handler(dto.handler())
                .code(code)
                .timeout(dto.timeout() != null ? dto.timeout() : defaultTimeout)
                .memorySize(dto.memorySize() != null ? dto.memorySize() : defaultMemorySize);

        if (dto.description() != null) {
            builder.description(dto.description());
        }

        if (dto.environmentVariables() != null && !dto.environmentVariables().isEmpty()) {
            builder.environment(Environment.builder()
                    .variables(dto.environmentVariables())
                    .build());
        }

        CreateFunctionResponse response = lambdaClient.createFunction(builder.build());
        return Map.of(
                "functionName", response.functionName(),
                "functionArn", response.functionArn(),
                "runtime", response.runtimeAsString(),
                "state", response.stateAsString() != null ? response.stateAsString() : "Active");
    }

    public Map<String, String> createInlineFunction(String functionName, String handler,
                                                     String description, byte[] zipBytes) {
        CreateFunctionDto dto = new CreateFunctionDto(
                functionName, handler, description, null, null, null);
        return createFunction(dto, zipBytes);
    }

    public List<Map<String, String>> listFunctions() {
        ListFunctionsResponse response = lambdaClient.listFunctions();
        return response.functions().stream()
                .map(f -> Map.of(
                        "functionName", f.functionName(),
                        "functionArn", f.functionArn(),
                        "runtime", f.runtimeAsString(),
                        "handler", f.handler(),
                        "lastModified", f.lastModified() != null ? f.lastModified() : "n/a"))
                .toList();
    }

    public Map<String, String> getFunctionConfiguration(String functionName) {
        GetFunctionConfigurationResponse response = lambdaClient.getFunctionConfiguration(
                GetFunctionConfigurationRequest.builder()
                        .functionName(functionName)
                        .build());

        Map<String, String> config = new HashMap<>();
        config.put("functionName", response.functionName());
        config.put("functionArn", response.functionArn());
        config.put("runtime", response.runtimeAsString());
        config.put("handler", response.handler());
        config.put("timeout", String.valueOf(response.timeout()));
        config.put("memorySize", String.valueOf(response.memorySize()));
        config.put("lastModified", response.lastModified() != null ? response.lastModified() : "n/a");

        if (response.environment() != null && response.environment().variables() != null) {
            config.put("envVarCount", String.valueOf(response.environment().variables().size()));
        }

        return config;
    }

    public Map<String, String> updateFunctionConfiguration(String functionName,
                                                            Integer timeout,
                                                            Integer memorySize,
                                                            Map<String, String> envVars) {
        UpdateFunctionConfigurationRequest.Builder builder =
                UpdateFunctionConfigurationRequest.builder()
                        .functionName(functionName);

        if (timeout != null) builder.timeout(timeout);
        if (memorySize != null) builder.memorySize(memorySize);
        if (envVars != null) {
            builder.environment(Environment.builder().variables(envVars).build());
        }

        UpdateFunctionConfigurationResponse response = lambdaClient.updateFunctionConfiguration(builder.build());
        return Map.of(
                "functionName", response.functionName(),
                "timeout", String.valueOf(response.timeout()),
                "memorySize", String.valueOf(response.memorySize()));
    }

    public Map<String, String> updateFunctionCode(String functionName, byte[] zipBytes) {
        UpdateFunctionCodeResponse response = lambdaClient.updateFunctionCode(
                UpdateFunctionCodeRequest.builder()
                        .functionName(functionName)
                        .zipFile(SdkBytes.fromByteArray(zipBytes))
                        .build());
        return Map.of(
                "functionName", response.functionName(),
                "codeSha256", response.codeSha256() != null ? response.codeSha256() : "n/a");
    }

    public void deleteFunction(String functionName) {
        lambdaClient.deleteFunction(DeleteFunctionRequest.builder()
                .functionName(functionName)
                .build());
    }
}
