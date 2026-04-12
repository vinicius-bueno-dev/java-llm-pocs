package dev.nameless.poc.sns.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;

import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {

    private final SnsClient snsClient;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    public SubscriptionService(SnsClient snsClient, SqsClient sqsClient) {
        this.snsClient = snsClient;
        this.sqsClient = sqsClient;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, String> subscribeSqs(String topicArn, String queueUrl,
                                             Map<String, Object> filterPolicy,
                                             boolean rawMessageDelivery) {
        String queueArn = getQueueArn(queueUrl);

        allowSnsToPublishToSqs(topicArn, queueArn, queueUrl);

        SubscribeRequest.Builder builder = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("sqs")
                .endpoint(queueArn);

        if (rawMessageDelivery) {
            builder.attributes(Map.of("RawMessageDelivery", "true"));
        }

        SubscribeResponse response = snsClient.subscribe(builder.build());
        String subscriptionArn = response.subscriptionArn();

        if (filterPolicy != null && !filterPolicy.isEmpty()) {
            setFilterPolicy(subscriptionArn, filterPolicy);
        }

        return Map.of(
                "subscriptionArn", subscriptionArn,
                "queueArn", queueArn);
    }

    public void setFilterPolicy(String subscriptionArn, Map<String, Object> filterPolicy) {
        try {
            String policyJson = objectMapper.writeValueAsString(filterPolicy);
            snsClient.setSubscriptionAttributes(SetSubscriptionAttributesRequest.builder()
                    .subscriptionArn(subscriptionArn)
                    .attributeName("FilterPolicy")
                    .attributeValue(policyJson)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize filter policy", e);
        }
    }

    public List<Map<String, String>> listSubscriptions(String topicArn) {
        ListSubscriptionsByTopicResponse response = snsClient.listSubscriptionsByTopic(
                ListSubscriptionsByTopicRequest.builder().topicArn(topicArn).build());

        return response.subscriptions().stream()
                .map(s -> Map.of(
                        "subscriptionArn", s.subscriptionArn(),
                        "protocol", s.protocol(),
                        "endpoint", s.endpoint()))
                .toList();
    }

    public Map<String, String> getSubscriptionAttributes(String subscriptionArn) {
        GetSubscriptionAttributesResponse response = snsClient.getSubscriptionAttributes(
                GetSubscriptionAttributesRequest.builder()
                        .subscriptionArn(subscriptionArn)
                        .build());
        return response.attributes();
    }

    public void setSubscriptionAttribute(String subscriptionArn, String attributeName,
                                          String attributeValue) {
        snsClient.setSubscriptionAttributes(SetSubscriptionAttributesRequest.builder()
                .subscriptionArn(subscriptionArn)
                .attributeName(attributeName)
                .attributeValue(attributeValue)
                .build());
    }

    public void unsubscribe(String subscriptionArn) {
        snsClient.unsubscribe(UnsubscribeRequest.builder()
                .subscriptionArn(subscriptionArn)
                .build());
    }

    public void configureDeadLetterQueue(String subscriptionArn, String dlqArn) {
        try {
            String redrivePolicy = objectMapper.writeValueAsString(
                    Map.of("deadLetterTargetArn", dlqArn));
            snsClient.setSubscriptionAttributes(SetSubscriptionAttributesRequest.builder()
                    .subscriptionArn(subscriptionArn)
                    .attributeName("RedrivePolicy")
                    .attributeValue(redrivePolicy)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize redrive policy", e);
        }
    }

    private String getQueueArn(String queueUrl) {
        return sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(QueueAttributeName.QUEUE_ARN)
                        .build())
                .attributes()
                .get(QueueAttributeName.QUEUE_ARN);
    }

    private void allowSnsToPublishToSqs(String topicArn, String queueArn, String queueUrl) {
        String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [{
                    "Sid": "AllowSNSPublish",
                    "Effect": "Allow",
                    "Principal": "*",
                    "Action": "sqs:SendMessage",
                    "Resource": "%s",
                    "Condition": {
                      "ArnEquals": { "aws:SourceArn": "%s" }
                    }
                  }]
                }
                """.formatted(queueArn, topicArn);

        sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(Map.of(QueueAttributeName.POLICY, policy))
                .build());
    }
}
