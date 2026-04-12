package dev.nameless.poc.eventdriven.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.util.List;
import java.util.Map;

@Service
public class EventBusService {

    private final EventBridgeClient eventBridgeClient;

    public EventBusService(EventBridgeClient eventBridgeClient) {
        this.eventBridgeClient = eventBridgeClient;
    }

    public Map<String, String> createEventBus(String busName) {
        CreateEventBusResponse response = eventBridgeClient.createEventBus(
                CreateEventBusRequest.builder().name(busName).build());
        return Map.of("eventBusArn", response.eventBusArn());
    }

    public List<Map<String, String>> listEventBuses() {
        ListEventBusesResponse response = eventBridgeClient.listEventBuses(
                ListEventBusesRequest.builder().build());
        return response.eventBuses().stream()
                .map(b -> Map.of(
                        "name", b.name(),
                        "arn", b.arn()))
                .toList();
    }

    public Map<String, String> describeEventBus(String busName) {
        DescribeEventBusResponse response = eventBridgeClient.describeEventBus(
                DescribeEventBusRequest.builder().name(busName).build());
        return Map.of(
                "name", response.name(),
                "arn", response.arn());
    }

    public void deleteEventBus(String busName) {
        eventBridgeClient.deleteEventBus(
                DeleteEventBusRequest.builder().name(busName).build());
    }
}
