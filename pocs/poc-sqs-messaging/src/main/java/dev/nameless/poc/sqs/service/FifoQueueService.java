package dev.nameless.poc.sqs.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.*;

@Service
public class FifoQueueService {

    private final SqsClient sqsClient;

    public FifoQueueService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public String createFifoQueue(String queueName, boolean contentBasedDedup) {
        if (!queueName.endsWith(".fifo")) {
            queueName += ".fifo";
        }
        return sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName(queueName)
                .attributes(Map.of(
                        QueueAttributeName.FIFO_QUEUE, "true",
                        QueueAttributeName.CONTENT_BASED_DEDUPLICATION, String.valueOf(contentBasedDedup)))
                .build()).queueUrl();
    }

    public List<Map<String, String>> sendOrderedMessages(String queueUrl, String groupId, List<String> messages) {
        List<Map<String, String>> results = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messages.get(i))
                    .messageGroupId(groupId)
                    .messageDeduplicationId(groupId + "-" + i + "-" + System.nanoTime())
                    .build());
            results.add(Map.of(
                    "messageId", response.messageId(),
                    "sequenceNumber", response.sequenceNumber() != null ? response.sequenceNumber() : "n/a",
                    "order", String.valueOf(i)));
        }
        return results;
    }

    public Map<String, Object> demonstrateDeduplication(String queueUrl, String groupId, String body) {
        String dedupId = "dedup-" + System.currentTimeMillis();

        SendMessageResponse first = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .messageGroupId(groupId)
                .messageDeduplicationId(dedupId)
                .build());

        SendMessageResponse second = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .messageGroupId(groupId)
                .messageDeduplicationId(dedupId)
                .build());

        return Map.of(
                "firstMessageId", first.messageId(),
                "secondMessageId", second.messageId(),
                "sameMessage", first.messageId().equals(second.messageId()),
                "deduplicationId", dedupId);
    }

    public List<Map<String, String>> receiveOrdered(String queueUrl, int maxMessages) {
        ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(maxMessages)
                .messageSystemAttributeNamesWithStrings("All")
                .build());

        List<Map<String, String>> results = new ArrayList<>();
        int order = 0;
        for (Message msg : response.messages()) {
            Map<String, String> result = new HashMap<>();
            result.put("order", String.valueOf(order++));
            result.put("messageId", msg.messageId());
            result.put("body", msg.body());
            result.put("sequenceNumber", msg.attributesAsStrings()
                    .getOrDefault("SequenceNumber", "n/a"));
            result.put("messageGroupId", msg.attributesAsStrings()
                    .getOrDefault("MessageGroupId", "n/a"));
            results.add(result);
        }
        return results;
    }

    public Map<String, Object> sendToMultipleGroups(String queueUrl, Map<String, List<String>> groupMessages) {
        Map<String, Object> results = new HashMap<>();
        groupMessages.forEach((groupId, messages) -> {
            List<String> messageIds = new ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messages.get(i))
                        .messageGroupId(groupId)
                        .messageDeduplicationId(groupId + "-" + i + "-" + System.nanoTime())
                        .build());
                messageIds.add(response.messageId());
            }
            results.put(groupId, messageIds);
        });
        return results;
    }
}
