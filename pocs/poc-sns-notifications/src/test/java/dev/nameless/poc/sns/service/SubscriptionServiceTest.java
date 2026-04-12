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

class SubscriptionServiceTest extends AbstractLocalStackTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SqsClient sqsClient;

    private String topicArn;
    private String queueUrl;

    @BeforeEach
    void setUp() {
        topicArn = topicService.createTopic("test-sub-topic-" + System.nanoTime(), null)
                .get("topicArn");
        queueUrl = sqsClient.createQueue(CreateQueueRequest.builder()
                        .queueName("test-sub-queue-" + System.nanoTime())
                        .build())
                .queueUrl();
    }

    @Test
    void shouldSubscribeSqsQueue() {
        Map<String, String> result = subscriptionService.subscribeSqs(
                topicArn, queueUrl, null, false);

        assertNotNull(result.get("subscriptionArn"));
        assertNotNull(result.get("queueArn"));
    }

    @Test
    void shouldSubscribeWithRawMessageDelivery() {
        Map<String, String> result = subscriptionService.subscribeSqs(
                topicArn, queueUrl, null, true);

        String subArn = result.get("subscriptionArn");
        Map<String, String> attrs = subscriptionService.getSubscriptionAttributes(subArn);
        assertEquals("true", attrs.get("RawMessageDelivery"));
    }

    @Test
    void shouldSubscribeWithFilterPolicy() {
        Map<String, Object> filterPolicy = Map.of(
                "eventType", List.of("order_created", "order_updated"));

        Map<String, String> result = subscriptionService.subscribeSqs(
                topicArn, queueUrl, filterPolicy, true);

        String subArn = result.get("subscriptionArn");
        Map<String, String> attrs = subscriptionService.getSubscriptionAttributes(subArn);
        assertNotNull(attrs.get("FilterPolicy"));
        assertTrue(attrs.get("FilterPolicy").contains("order_created"));
    }

    @Test
    void shouldListSubscriptions() {
        subscriptionService.subscribeSqs(topicArn, queueUrl, null, false);

        List<Map<String, String>> subs = subscriptionService.listSubscriptions(topicArn);

        assertFalse(subs.isEmpty());
        assertEquals("sqs", subs.getFirst().get("protocol"));
    }

    @Test
    void shouldUnsubscribe() {
        Map<String, String> result = subscriptionService.subscribeSqs(
                topicArn, queueUrl, null, false);
        String subArn = result.get("subscriptionArn");

        subscriptionService.unsubscribe(subArn);

        List<Map<String, String>> subs = subscriptionService.listSubscriptions(topicArn);
        boolean found = subs.stream()
                .anyMatch(s -> s.get("subscriptionArn").equals(subArn));
        assertFalse(found);
    }

    @Test
    void shouldUpdateFilterPolicy() {
        Map<String, String> result = subscriptionService.subscribeSqs(
                topicArn, queueUrl, null, true);
        String subArn = result.get("subscriptionArn");

        Map<String, Object> newFilter = Map.of("priority", List.of("high", "critical"));
        subscriptionService.setFilterPolicy(subArn, newFilter);

        Map<String, String> attrs = subscriptionService.getSubscriptionAttributes(subArn);
        assertTrue(attrs.get("FilterPolicy").contains("high"));
    }
}
