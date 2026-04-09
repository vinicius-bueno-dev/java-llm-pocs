package dev.nameless.poc.sqs.service;

import dev.nameless.poc.sqs.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QueueOperationsServiceTest extends AbstractLocalStackTest {

    @Autowired
    private QueueOperationsService service;

    @Test
    void shouldCreateAndListStandardQueue() {
        String name = "test-queue-" + System.currentTimeMillis();
        String url = service.createStandardQueue(name, null);
        assertThat(url).contains(name);

        List<String> queues = service.listQueues("test-queue-");
        assertThat(queues).anyMatch(q -> q.contains(name));
    }

    @Test
    void shouldCreateFifoQueue() {
        String name = "test-fifo-" + System.currentTimeMillis() + ".fifo";
        String url = service.createFifoQueue(name, true);
        assertThat(url).contains(name);
    }

    @Test
    void shouldGetQueueUrl() {
        String name = "test-url-" + System.currentTimeMillis();
        String created = service.createStandardQueue(name, null);
        String fetched = service.getQueueUrl(name);
        assertThat(fetched).isEqualTo(created);
    }

    @Test
    void shouldGetAndSetAttributes() {
        String name = "test-attrs-" + System.currentTimeMillis();
        String url = service.createStandardQueue(name, null);

        service.setQueueAttributes(url, Map.of("VisibilityTimeout", "60"));
        Map<String, String> attrs = service.getQueueAttributes(url);
        assertThat(attrs.get("VisibilityTimeout")).isEqualTo("60");
    }

    @Test
    void shouldDeleteQueue() {
        String name = "test-delete-" + System.currentTimeMillis();
        String url = service.createStandardQueue(name, null);

        service.deleteQueue(url);
        List<String> queues = service.listQueues(name);
        assertThat(queues).noneMatch(q -> q.contains(name));
    }
}
