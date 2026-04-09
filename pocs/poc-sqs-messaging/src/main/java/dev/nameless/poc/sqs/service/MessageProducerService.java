package dev.nameless.poc.sqs.service;

import dev.nameless.poc.sqs.dto.SendMessageDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.*;
import java.util.stream.IntStream;

@Service
public class MessageProducerService {

    private final SqsClient sqsClient;

    public MessageProducerService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public Map<String, String> sendMessage(String queueUrl, String body) {
        SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build());
        return Map.of(
                "messageId", response.messageId(),
                "md5OfBody", response.md5OfMessageBody());
    }

    public Map<String, String> sendMessageWithAttributes(String queueUrl, String body,
                                                          Map<String, String> attributes) {
        Map<String, MessageAttributeValue> msgAttributes = new HashMap<>();
        attributes.forEach((k, v) -> msgAttributes.put(k, MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(v)
                .build()));

        SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .messageAttributes(msgAttributes)
                .build());
        return Map.of(
                "messageId", response.messageId(),
                "md5OfBody", response.md5OfMessageBody());
    }

    public Map<String, String> sendMessageWithDelay(String queueUrl, String body, int delaySeconds) {
        SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .delaySeconds(delaySeconds)
                .build());
        return Map.of(
                "messageId", response.messageId(),
                "delaySeconds", String.valueOf(delaySeconds));
    }

    public Map<String, String> sendFifoMessage(String queueUrl, String body,
                                                String groupId, String deduplicationId) {
        SendMessageRequest.Builder builder = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .messageGroupId(groupId);

        if (deduplicationId != null) {
            builder.messageDeduplicationId(deduplicationId);
        }

        SendMessageResponse response = sqsClient.sendMessage(builder.build());
        return Map.of(
                "messageId", response.messageId(),
                "sequenceNumber", response.sequenceNumber() != null ? response.sequenceNumber() : "n/a");
    }

    public Map<String, Object> sendMessageBatch(String queueUrl, List<SendMessageDto> messages) {
        List<SendMessageBatchRequestEntry> entries = IntStream.range(0, messages.size())
                .mapToObj(i -> {
                    SendMessageDto msg = messages.get(i);
                    SendMessageBatchRequestEntry.Builder entry = SendMessageBatchRequestEntry.builder()
                            .id(String.valueOf(i))
                            .messageBody(msg.body());

                    if (msg.delaySeconds() != null) {
                        entry.delaySeconds(msg.delaySeconds());
                    }
                    if (msg.attributes() != null) {
                        Map<String, MessageAttributeValue> attrs = new HashMap<>();
                        msg.attributes().forEach((k, v) -> attrs.put(k, MessageAttributeValue.builder()
                                .dataType("String").stringValue(v).build()));
                        entry.messageAttributes(attrs);
                    }
                    return entry.build();
                })
                .toList();

        SendMessageBatchResponse response = sqsClient.sendMessageBatch(SendMessageBatchRequest.builder()
                .queueUrl(queueUrl)
                .entries(entries)
                .build());

        List<String> successful = response.successful().stream()
                .map(SendMessageBatchResultEntry::messageId)
                .toList();
        List<String> failed = response.failed().stream()
                .map(BatchResultErrorEntry::message)
                .toList();

        return Map.of("successful", successful, "failed", failed);
    }

    public Map<String, Object> sendFifoBatch(String queueUrl, List<SendMessageDto> messages) {
        List<SendMessageBatchRequestEntry> entries = IntStream.range(0, messages.size())
                .mapToObj(i -> {
                    SendMessageDto msg = messages.get(i);
                    SendMessageBatchRequestEntry.Builder entry = SendMessageBatchRequestEntry.builder()
                            .id(String.valueOf(i))
                            .messageBody(msg.body())
                            .messageGroupId(msg.groupId());

                    if (msg.deduplicationId() != null) {
                        entry.messageDeduplicationId(msg.deduplicationId());
                    }
                    return entry.build();
                })
                .toList();

        SendMessageBatchResponse response = sqsClient.sendMessageBatch(SendMessageBatchRequest.builder()
                .queueUrl(queueUrl)
                .entries(entries)
                .build());

        List<String> successful = response.successful().stream()
                .map(SendMessageBatchResultEntry::messageId)
                .toList();

        return Map.of("successful", successful, "count", successful.size());
    }
}
