package dev.nameless.poc.sqs.service;

import dev.nameless.poc.sqs.AbstractLocalStackTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessageConsumerServiceTest extends AbstractLocalStackTest {

    @Autowired
    private MessageConsumerService consumerService;

    @Autowired
    private MessageProducerService producerService;

    @Autowired
    private QueueOperationsService queueService;

    private String queueUrl;

    @BeforeEach
    void setUp() {
        queueUrl = queueService.createStandardQueue("test-consumer-" + System.currentTimeMillis(), null);
    }

    @Test
    void shouldReceiveAndDeleteMessage() {
        producerService.sendMessage(queueUrl, "Test receive");

        List<Map<String, String>> messages = consumerService.receiveMessages(queueUrl, 1, 1);
        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().get("body")).isEqualTo("Test receive");

        consumerService.deleteMessage(queueUrl, messages.getFirst().get("receiptHandle"));

        List<Map<String, String>> afterDelete = consumerService.receiveMessages(queueUrl, 1, 1);
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void shouldChangeVisibilityTimeout() {
        producerService.sendMessage(queueUrl, "Visibility test");

        List<Map<String, String>> messages = consumerService.receiveMessages(queueUrl, 1, 1);
        assertThat(messages).isNotEmpty();

        String receiptHandle = messages.getFirst().get("receiptHandle");
        consumerService.changeVisibilityTimeout(queueUrl, receiptHandle, 0);

        List<Map<String, String>> again = consumerService.receiveMessages(queueUrl, 1, 1);
        assertThat(again).isNotEmpty();
    }
}
