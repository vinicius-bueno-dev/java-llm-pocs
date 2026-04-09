package dev.nameless.poc.sqs.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageConsumerService {

    private final SqsClient sqsClient;

    public MessageConsumerService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public List<Map<String, String>> receiveMessages(String queueUrl, int maxMessages, int waitTimeSeconds) {
        ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(maxMessages)
                .waitTimeSeconds(waitTimeSeconds)
                .messageSystemAttributeNamesWithStrings("All")
                .messageAttributeNames("All")
                .build());

        return response.messages().stream()
                .map(msg -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("messageId", msg.messageId());
                    result.put("body", msg.body());
                    result.put("receiptHandle", msg.receiptHandle());
                    result.put("md5OfBody", msg.md5OfBody());

                    msg.attributesAsStrings().forEach((k, v) -> result.put("sys_" + k, v));

                    msg.messageAttributes().forEach((k, v) ->
                            result.put("attr_" + k, v.stringValue()));

                    return result;
                })
                .toList();
    }

    public void deleteMessage(String queueUrl, String receiptHandle) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build());
    }

    public Map<String, Object> deleteMessageBatch(String queueUrl, List<String> receiptHandles) {
        List<DeleteMessageBatchRequestEntry> entries = new java.util.ArrayList<>();
        for (int i = 0; i < receiptHandles.size(); i++) {
            entries.add(DeleteMessageBatchRequestEntry.builder()
                    .id(String.valueOf(i))
                    .receiptHandle(receiptHandles.get(i))
                    .build());
        }

        DeleteMessageBatchResponse response = sqsClient.deleteMessageBatch(
                DeleteMessageBatchRequest.builder()
                        .queueUrl(queueUrl)
                        .entries(entries)
                        .build());

        return Map.of(
                "successful", response.successful().size(),
                "failed", response.failed().size());
    }

    public void changeVisibilityTimeout(String queueUrl, String receiptHandle, int visibilityTimeout) {
        sqsClient.changeMessageVisibility(ChangeMessageVisibilityRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .visibilityTimeout(visibilityTimeout)
                .build());
    }

    public Map<String, Object> changeVisibilityTimeoutBatch(String queueUrl,
                                                             Map<String, Integer> receiptHandleToTimeout) {
        List<ChangeMessageVisibilityBatchRequestEntry> entries = new java.util.ArrayList<>();
        int id = 0;
        for (var entry : receiptHandleToTimeout.entrySet()) {
            entries.add(ChangeMessageVisibilityBatchRequestEntry.builder()
                    .id(String.valueOf(id++))
                    .receiptHandle(entry.getKey())
                    .visibilityTimeout(entry.getValue())
                    .build());
        }

        ChangeMessageVisibilityBatchResponse response = sqsClient.changeMessageVisibilityBatch(
                ChangeMessageVisibilityBatchRequest.builder()
                        .queueUrl(queueUrl)
                        .entries(entries)
                        .build());

        return Map.of(
                "successful", response.successful().size(),
                "failed", response.failed().size());
    }
}
