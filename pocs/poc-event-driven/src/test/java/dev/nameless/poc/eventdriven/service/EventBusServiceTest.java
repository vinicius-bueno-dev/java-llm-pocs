package dev.nameless.poc.eventdriven.service;

import dev.nameless.poc.eventdriven.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventBusServiceTest extends AbstractLocalStackTest {

    @Autowired
    private EventBusService eventBusService;

    @Test
    void shouldCreateCustomEventBus() {
        Map<String, String> result = eventBusService.createEventBus("test-bus-" + System.nanoTime());
        assertNotNull(result.get("eventBusArn"));
    }

    @Test
    void shouldListEventBuses() {
        eventBusService.createEventBus("test-list-bus-" + System.nanoTime());
        List<Map<String, String>> buses = eventBusService.listEventBuses();
        assertFalse(buses.isEmpty());
    }

    @Test
    void shouldDescribeEventBus() {
        String busName = "test-describe-bus-" + System.nanoTime();
        eventBusService.createEventBus(busName);

        Map<String, String> bus = eventBusService.describeEventBus(busName);
        assertEquals(busName, bus.get("name"));
    }

    @Test
    void shouldDeleteEventBus() {
        String busName = "test-delete-bus-" + System.nanoTime();
        eventBusService.createEventBus(busName);
        eventBusService.deleteEventBus(busName);

        List<Map<String, String>> buses = eventBusService.listEventBuses();
        boolean found = buses.stream().anyMatch(b -> b.get("name").equals(busName));
        assertFalse(found);
    }
}
