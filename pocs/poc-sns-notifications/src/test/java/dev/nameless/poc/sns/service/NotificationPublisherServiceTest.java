package dev.nameless.poc.sns.service;

import dev.nameless.poc.sns.AbstractLocalStackTest;
import dev.nameless.poc.sns.dto.PublishMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationPublisherServiceTest extends AbstractLocalStackTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private NotificationPublisherService publisherService;

    private String topicArn;
    private String fifoTopicArn;

    @BeforeEach
    void setUp() {
        topicArn = topicService.createTopic("test-publish-topic", null).get("topicArn");
        fifoTopicArn = topicService.createFifoTopic("test-publish-fifo", true).get("topicArn");
    }

    @Test
    void shouldPublishSimpleMessage() {
        Map<String, String> result = publisherService.publish(topicArn, "Hello SNS", null);

        assertNotNull(result.get("messageId"));
    }

    @Test
    void shouldPublishWithSubject() {
        Map<String, String> result = publisherService.publish(topicArn, "Body here", "Test Subject");

        assertNotNull(result.get("messageId"));
    }

    @Test
    void shouldPublishWithAttributes() {
        Map<String, String> attributes = Map.of("eventType", "order_created", "priority", "high");
        Map<String, String> result = publisherService.publishWithAttributes(
                topicArn, "Order created", "New Order", attributes);

        assertNotNull(result.get("messageId"));
        assertEquals("2", result.get("attributeCount"));
    }

    @Test
    void shouldPublishFifoMessage() {
        Map<String, String> result = publisherService.publishFifo(
                fifoTopicArn, "FIFO message", "group-1", "dedup-1");

        assertNotNull(result.get("messageId"));
    }

    @Test
    void shouldPublishBatch() {
        List<PublishMessageDto> messages = List.of(
                new PublishMessageDto("Message 1", "Subject 1", null, null, null),
                new PublishMessageDto("Message 2", "Subject 2", null, null, null),
                new PublishMessageDto("Message 3", null, Map.of("type", "test"), null, null));

        Map<String, Object> result = publisherService.publishBatch(topicArn, messages);

        @SuppressWarnings("unchecked")
        List<String> successful = (List<String>) result.get("successful");
        assertEquals(3, successful.size());
    }
}
