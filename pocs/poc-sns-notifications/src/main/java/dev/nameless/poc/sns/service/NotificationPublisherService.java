package dev.nameless.poc.sns.service;

import dev.nameless.poc.sns.dto.PublishMessageDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.*;
import java.util.stream.IntStream;

@Service
public class NotificationPublisherService {

    private final SnsClient snsClient;

    public NotificationPublisherService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public Map<String, String> publish(String topicArn, String message, String subject) {
        PublishRequest.Builder builder = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message);

        if (subject != null) {
            builder.subject(subject);
        }

        PublishResponse response = snsClient.publish(builder.build());
        return Map.of(
                "messageId", response.messageId(),
                "sequenceNumber", response.sequenceNumber() != null ? response.sequenceNumber() : "n/a");
    }

    public Map<String, String> publishWithAttributes(String topicArn, String message,
                                                      String subject, Map<String, String> attributes) {
        Map<String, MessageAttributeValue> msgAttributes = new HashMap<>();
        attributes.forEach((k, v) -> msgAttributes.put(k, MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(v)
                .build()));

        PublishRequest.Builder builder = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageAttributes(msgAttributes);

        if (subject != null) {
            builder.subject(subject);
        }

        PublishResponse response = snsClient.publish(builder.build());
        return Map.of(
                "messageId", response.messageId(),
                "attributeCount", String.valueOf(attributes.size()));
    }

    public Map<String, String> publishFifo(String topicArn, String message,
                                            String groupId, String deduplicationId) {
        PublishRequest.Builder builder = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageGroupId(groupId);

        if (deduplicationId != null) {
            builder.messageDeduplicationId(deduplicationId);
        }

        PublishResponse response = snsClient.publish(builder.build());
        return Map.of(
                "messageId", response.messageId(),
                "sequenceNumber", response.sequenceNumber() != null ? response.sequenceNumber() : "n/a");
    }

    public Map<String, Object> publishBatch(String topicArn, List<PublishMessageDto> messages) {
        List<PublishBatchRequestEntry> entries = IntStream.range(0, messages.size())
                .mapToObj(i -> {
                    PublishMessageDto msg = messages.get(i);
                    PublishBatchRequestEntry.Builder entry = PublishBatchRequestEntry.builder()
                            .id(String.valueOf(i))
                            .message(msg.message());

                    if (msg.subject() != null) {
                        entry.subject(msg.subject());
                    }
                    if (msg.attributes() != null) {
                        Map<String, MessageAttributeValue> attrs = new HashMap<>();
                        msg.attributes().forEach((k, v) -> attrs.put(k,
                                MessageAttributeValue.builder().dataType("String").stringValue(v).build()));
                        entry.messageAttributes(attrs);
                    }
                    if (msg.groupId() != null) {
                        entry.messageGroupId(msg.groupId());
                    }
                    if (msg.deduplicationId() != null) {
                        entry.messageDeduplicationId(msg.deduplicationId());
                    }
                    return entry.build();
                })
                .toList();

        PublishBatchResponse response = snsClient.publishBatch(PublishBatchRequest.builder()
                .topicArn(topicArn)
                .publishBatchRequestEntries(entries)
                .build());

        List<String> successful = response.successful().stream()
                .map(PublishBatchResultEntry::messageId)
                .toList();
        List<String> failed = response.failed().stream()
                .map(BatchResultErrorEntry::message)
                .toList();

        return Map.of("successful", successful, "failed", failed);
    }
}
