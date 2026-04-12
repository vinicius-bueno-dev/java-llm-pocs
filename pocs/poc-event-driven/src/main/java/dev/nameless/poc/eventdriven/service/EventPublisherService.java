package dev.nameless.poc.eventdriven.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nameless.poc.eventdriven.dto.PutEventDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventPublisherService {

    private final EventBridgeClient eventBridgeClient;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    public EventPublisherService(EventBridgeClient eventBridgeClient, SqsClient sqsClient) {
        this.eventBridgeClient = eventBridgeClient;
        this.sqsClient = sqsClient;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> putEvent(String busName, PutEventDto dto) {
        String detailJson;
        try {
            detailJson = objectMapper.writeValueAsString(dto.detail());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event detail", e);
        }

        PutEventsResponse response = eventBridgeClient.putEvents(PutEventsRequest.builder()
                .entries(PutEventsRequestEntry.builder()
                        .eventBusName(busName)
                        .source(dto.source())
                        .detailType(dto.detailType())
                        .detail(detailJson)
                        .build())
                .build());

        Map<String, Object> result = new HashMap<>();
        result.put("failedEntryCount", response.failedEntryCount());

        List<Map<String, String>> entries = response.entries().stream()
                .map(e -> {
                    Map<String, String> entry = new HashMap<>();
                    if (e.eventId() != null) entry.put("eventId", e.eventId());
                    if (e.errorCode() != null) entry.put("errorCode", e.errorCode());
                    if (e.errorMessage() != null) entry.put("errorMessage", e.errorMessage());
                    return entry;
                })
                .toList();
        result.put("entries", entries);
        return result;
    }

    public Map<String, Object> putEventBatch(String busName, List<PutEventDto> events) {
        List<PutEventsRequestEntry> entries = events.stream()
                .map(dto -> {
                    String detailJson;
                    try {
                        detailJson = objectMapper.writeValueAsString(dto.detail());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to serialize event detail", e);
                    }
                    return PutEventsRequestEntry.builder()
                            .eventBusName(busName)
                            .source(dto.source())
                            .detailType(dto.detailType())
                            .detail(detailJson)
                            .build();
                })
                .toList();

        PutEventsResponse response = eventBridgeClient.putEvents(PutEventsRequest.builder()
                .entries(entries)
                .build());

        return Map.of(
                "failedEntryCount", response.failedEntryCount(),
                "totalEntries", entries.size());
    }

    public Map<String, Object> putEventAndVerifyDelivery(String busName, PutEventDto dto,
                                                          List<String> targetQueueUrls) {
        Map<String, Object> publishResult = putEvent(busName, dto);

        Map<String, Object> result = new HashMap<>(publishResult);
        Map<String, Object> deliveryResults = new HashMap<>();

        for (String queueUrl : targetQueueUrls) {
            List<Message> received = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build()).messages();

            deliveryResults.put(queueUrl, Map.of(
                    "received", !received.isEmpty(),
                    "messageCount", received.size(),
                    "messages", received.stream()
                            .map(m -> Map.of("messageId", m.messageId(), "body", m.body()))
                            .toList()));
        }

        result.put("deliveryResults", deliveryResults);
        return result;
    }
}
