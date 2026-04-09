package dev.nameless.poc.sqs.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class DelayQueueService {

    private final SqsClient sqsClient;

    public DelayQueueService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public String createDelayQueue(String queueName, int delaySeconds) {
        return sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName(queueName)
                .attributes(Map.of(QueueAttributeName.DELAY_SECONDS, String.valueOf(delaySeconds)))
                .build()).queueUrl();
    }

    public Map<String, String> sendWithMessageDelay(String queueUrl, String body, int delaySeconds) {
        SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .delaySeconds(delaySeconds)
                .build());
        return Map.of(
                "messageId", response.messageId(),
                "delaySeconds", String.valueOf(delaySeconds));
    }

    public Map<String, Object> demonstrateDelay(String queueUrl, String body, int delaySeconds) {
        Map<String, Object> result = new HashMap<>();

        SendMessageResponse sendResponse = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .delaySeconds(delaySeconds)
                .build());
        result.put("messageId", sendResponse.messageId());
        result.put("delaySeconds", delaySeconds);

        ReceiveMessageResponse immediateReceive = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(0)
                .build());
        result.put("immediateReceiveCount", immediateReceive.messages().size());
        result.put("immediateReceiveEmpty", immediateReceive.messages().isEmpty());

        return result;
    }

    public Map<String, String> getDelayConfig(String queueUrl) {
        GetQueueAttributesResponse response = sqsClient.getQueueAttributes(
                GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(QueueAttributeName.DELAY_SECONDS)
                        .build());
        return Map.of("delaySeconds", response.attributesAsStrings()
                .getOrDefault(QueueAttributeName.DELAY_SECONDS.toString(), "0"));
    }
}
