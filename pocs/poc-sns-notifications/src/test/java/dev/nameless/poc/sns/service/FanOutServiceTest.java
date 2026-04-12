package dev.nameless.poc.sns.service;

import dev.nameless.poc.sns.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FanOutServiceTest extends AbstractLocalStackTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private FanOutService fanOutService;

    @Autowired
    private SqsClient sqsClient;

    private String topicArn;
    private String ordersQueueUrl;
    private String analyticsQueueUrl;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());
        topicArn = topicService.createTopic("test-fanout-topic-" + suffix, null).get("topicArn");

        ordersQueueUrl = sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName("test-orders-" + suffix).build()).queueUrl();
        analyticsQueueUrl = sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName("test-analytics-" + suffix).build()).queueUrl();

        // Subscribe both queues with raw delivery
        subscriptionService.subscribeSqs(topicArn, ordersQueueUrl, null, true);
        subscriptionService.subscribeSqs(topicArn, analyticsQueueUrl, null, true);
    }

    @Test
    void shouldFanOutToMultipleQueues() {
        Map<String, Object> result = fanOutService.publishAndVerifyFanOut(
                topicArn, "Fan-out test message", null,
                List.of(ordersQueueUrl, analyticsQueueUrl));

        assertNotNull(result.get("messageId"));

        @SuppressWarnings("unchecked")
        Map<String, Object> deliveryResults = (Map<String, Object>) result.get("deliveryResults");
        assertNotNull(deliveryResults);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterMessagesBasedOnEventType() {
        String suffix = String.valueOf(System.nanoTime());
        String filterTopicArn = topicService.createTopic("test-filter-topic-" + suffix, null)
                .get("topicArn");

        String ordersQueue = sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName("test-filter-orders-" + suffix).build()).queueUrl();
        String analyticsQueue = sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName("test-filter-analytics-" + suffix).build()).queueUrl();

        // Orders queue only receives order events
        subscriptionService.subscribeSqs(filterTopicArn, ordersQueue,
                Map.of("eventType", List.of("order_created", "order_updated")), true);

        // Analytics queue receives all events
        subscriptionService.subscribeSqs(filterTopicArn, analyticsQueue, null, true);

        // Publish an order event
        Map<String, Object> result = fanOutService.demonstrateFilteredFanOut(
                filterTopicArn, "Order created!", "order_created",
                List.of(ordersQueue, analyticsQueue));

        assertNotNull(result.get("messageId"));
        assertEquals("order_created", result.get("eventType"));

        Map<String, Object> deliveryResults = (Map<String, Object>) result.get("deliveryResults");
        assertNotNull(deliveryResults);
    }
}
