package dev.nameless.poc.sqs.service;

import dev.nameless.poc.sqs.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FifoQueueServiceTest extends AbstractLocalStackTest {

    @Autowired
    private FifoQueueService fifoService;

    @Test
    void shouldCreateFifoQueue() {
        String name = "test-fifo-create-" + System.currentTimeMillis();
        String url = fifoService.createFifoQueue(name, true);
        assertThat(url).contains(name + ".fifo");
    }

    @Test
    void shouldSendAndReceiveOrdered() {
        String url = fifoService.createFifoQueue(
                "test-fifo-order-" + System.currentTimeMillis(), true);

        List<String> messages = List.of("first", "second", "third");
        fifoService.sendOrderedMessages(url, "group-1", messages);

        List<Map<String, String>> received = fifoService.receiveOrdered(url, 10);
        assertThat(received).hasSizeGreaterThanOrEqualTo(1);
        assertThat(received.getFirst().get("body")).isEqualTo("first");
    }

    @Test
    void shouldDemonstrateDeduplication() {
        String url = fifoService.createFifoQueue(
                "test-fifo-dedup-" + System.currentTimeMillis(), false);

        Map<String, Object> result = fifoService.demonstrateDeduplication(url, "group-dedup", "same message");
        assertThat(result.get("sameMessage")).isEqualTo(true);
    }

    @Test
    void shouldSendToMultipleGroups() {
        String url = fifoService.createFifoQueue(
                "test-fifo-multi-" + System.currentTimeMillis(), true);

        Map<String, List<String>> groups = Map.of(
                "group-a", List.of("a1", "a2"),
                "group-b", List.of("b1", "b2"));

        Map<String, Object> result = fifoService.sendToMultipleGroups(url, groups);
        assertThat(result).containsKeys("group-a", "group-b");
    }
}
