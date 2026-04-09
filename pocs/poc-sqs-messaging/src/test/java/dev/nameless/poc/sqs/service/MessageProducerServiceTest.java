package dev.nameless.poc.sqs.service;

import dev.nameless.poc.sqs.AbstractLocalStackTest;
import dev.nameless.poc.sqs.dto.SendMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessageProducerServiceTest extends AbstractLocalStackTest {

    @Autowired
    private MessageProducerService producerService;

    @Autowired
    private QueueOperationsService queueService;

    private String queueUrl;

    @BeforeEach
    void setUp() {
        queueUrl = queueService.createStandardQueue("test-producer-" + System.currentTimeMillis(), null);
    }

    @Test
    void shouldSendSimpleMessage() {
        Map<String, String> result = producerService.sendMessage(queueUrl, "Hello SQS");
        assertThat(result).containsKey("messageId");
        assertThat(result.get("messageId")).isNotBlank();
    }

    @Test
    void shouldSendMessageWithAttributes() {
        Map<String, String> attrs = Map.of("env", "test", "priority", "high");
        Map<String, String> result = producerService.sendMessageWithAttributes(queueUrl, "Test body", attrs);
        assertThat(result).containsKey("messageId");
    }

    @Test
    void shouldSendMessageWithDelay() {
        Map<String, String> result = producerService.sendMessageWithDelay(queueUrl, "Delayed msg", 1);
        assertThat(result.get("delaySeconds")).isEqualTo("1");
    }

    @Test
    void shouldSendBatch() {
        List<SendMessageDto> messages = List.of(
                new SendMessageDto("msg-1", null, null, null, null),
                new SendMessageDto("msg-2", null, null, null, null),
                new SendMessageDto("msg-3", null, null, null, null));

        Map<String, Object> result = producerService.sendMessageBatch(queueUrl, messages);
        @SuppressWarnings("unchecked")
        List<String> successful = (List<String>) result.get("successful");
        assertThat(successful).hasSize(3);
    }
}
