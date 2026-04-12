package dev.nameless.poc.lambda.service;

import dev.nameless.poc.lambda.dto.EventSourceMappingDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.util.List;
import java.util.Map;

@Service
public class EventSourceMappingService {

    private final LambdaClient lambdaClient;

    public EventSourceMappingService(LambdaClient lambdaClient) {
        this.lambdaClient = lambdaClient;
    }

    public Map<String, String> createEventSourceMapping(EventSourceMappingDto dto) {
        CreateEventSourceMappingResponse response = lambdaClient.createEventSourceMapping(
                CreateEventSourceMappingRequest.builder()
                        .functionName(dto.functionName())
                        .eventSourceArn(dto.eventSourceArn())
                        .batchSize(dto.batchSize() > 0 ? dto.batchSize() : 10)
                        .enabled(dto.enabled())
                        .build());

        return Map.of(
                "uuid", response.uuid(),
                "functionArn", response.functionArn(),
                "eventSourceArn", response.eventSourceArn(),
                "state", response.state() != null ? response.state() : "Enabled");
    }

    public List<Map<String, String>> listEventSourceMappings(String functionName) {
        ListEventSourceMappingsResponse response = lambdaClient.listEventSourceMappings(
                ListEventSourceMappingsRequest.builder()
                        .functionName(functionName)
                        .build());

        return response.eventSourceMappings().stream()
                .map(m -> Map.of(
                        "uuid", m.uuid(),
                        "eventSourceArn", m.eventSourceArn(),
                        "state", m.state() != null ? m.state() : "n/a",
                        "batchSize", String.valueOf(m.batchSize())))
                .toList();
    }

    public Map<String, String> updateEventSourceMapping(String uuid, Integer batchSize, boolean enabled) {
        UpdateEventSourceMappingRequest.Builder builder = UpdateEventSourceMappingRequest.builder()
                .uuid(uuid)
                .enabled(enabled);

        if (batchSize != null) {
            builder.batchSize(batchSize);
        }

        UpdateEventSourceMappingResponse response = lambdaClient.updateEventSourceMapping(builder.build());
        return Map.of(
                "uuid", response.uuid(),
                "state", response.state() != null ? response.state() : "n/a",
                "batchSize", String.valueOf(response.batchSize()));
    }

    public void deleteEventSourceMapping(String uuid) {
        lambdaClient.deleteEventSourceMapping(DeleteEventSourceMappingRequest.builder()
                .uuid(uuid)
                .build());
    }
}
