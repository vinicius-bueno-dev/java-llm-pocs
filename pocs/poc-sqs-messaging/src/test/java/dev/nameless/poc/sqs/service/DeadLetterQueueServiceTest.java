package dev.nameless.poc.sqs.service;

import dev.nameless.poc.sqs.AbstractLocalStackTest;
import dev.nameless.poc.sqs.dto.RedriveConfigDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeadLetterQueueServiceTest extends AbstractLocalStackTest {

    @Autowired
    private DeadLetterQueueService dlqService;

    @Autowired
    private QueueOperationsService queueService;

    @Autowired
    private MessageProducerService producerService;

    @Test
    void shouldConfigureAndGetRedrivePolicy() {
        String ts = String.valueOf(System.currentTimeMillis());
        String dlqUrl = queueService.createStandardQueue("test-dlq-" + ts, null);
        String queueUrl = queueService.createStandardQueue("test-src-" + ts, null);

        Map<String, String> dlqAttrs = queueService.getQueueAttributes(dlqUrl);
        String dlqArn = dlqAttrs.get("QueueArn");

        dlqService.configureRedrivePolicy(queueUrl, new RedriveConfigDto(dlqArn, 2));

        Map<String, String> policy = dlqService.getRedrivePolicy(queueUrl);
        assertThat(policy.get("redrivePolicy")).contains("maxReceiveCount");
        assertThat(policy.get("redrivePolicy")).contains(dlqArn);
    }

    @Test
    void shouldListDlqMessages() {
        String ts = String.valueOf(System.currentTimeMillis());
        String dlqUrl = queueService.createStandardQueue("test-dlq-list-" + ts, null);

        producerService.sendMessage(dlqUrl, "DLQ test message");

        var messages = dlqService.listDlqMessages(dlqUrl, 10);
        assertThat(messages).isNotEmpty();
        assertThat(messages.getFirst().get("body")).isEqualTo("DLQ test message");
    }
}
