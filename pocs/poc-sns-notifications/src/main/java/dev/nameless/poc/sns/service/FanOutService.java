package dev.nameless.poc.sns.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FanOutService {

    private final SnsClient snsClient;
    private final SqsClient sqsClient;

    public FanOutService(SnsClient snsClient, SqsClient sqsClient) {
        this.snsClient = snsClient;
        this.sqsClient = sqsClient;
    }

    public Map<String, Object> publishAndVerifyFanOut(String topicArn, String message,
                                                       Map<String, String> attributes,
                                                       List<String> subscriberQueueUrls) {
        Map<String, MessageAttributeValue> msgAttributes = new HashMap<>();
        if (attributes != null) {
            attributes.forEach((k, v) -> msgAttributes.put(k, MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(v)
                    .build()));
        }

        PublishResponse publishResponse = snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageAttributes(msgAttributes)
                .build());

        Map<String, Object> result = new HashMap<>();
        result.put("messageId", publishResponse.messageId());
        result.put("topicArn", topicArn);

        Map<String, Object> deliveryResults = new HashMap<>();
        for (String queueUrl : subscriberQueueUrls) {
            List<Message> received = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build()).messages();

            deliveryResults.put(queueUrl, Map.of(
                    "messageCount", received.size(),
                    "messages", received.stream()
                            .map(m -> Map.of("messageId", m.messageId(), "body", m.body()))
                            .toList()));
        }

        result.put("deliveryResults", deliveryResults);
        return result;
    }

    public Map<String, Object> demonstrateFilteredFanOut(String topicArn, String message,
                                                          String eventType,
                                                          List<String> subscriberQueueUrls) {
        PublishResponse publishResponse = snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageAttributes(Map.of(
                        "eventType", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(eventType)
                                .build()))
                .build());

        Map<String, Object> result = new HashMap<>();
        result.put("messageId", publishResponse.messageId());
        result.put("eventType", eventType);

        Map<String, Object> deliveryResults = new HashMap<>();
        for (String queueUrl : subscriberQueueUrls) {
            List<Message> received = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(3)
                    .build()).messages();

            deliveryResults.put(queueUrl, Map.of(
                    "received", !received.isEmpty(),
                    "messageCount", received.size()));
        }

        result.put("deliveryResults", deliveryResults);
        return result;
    }
}
