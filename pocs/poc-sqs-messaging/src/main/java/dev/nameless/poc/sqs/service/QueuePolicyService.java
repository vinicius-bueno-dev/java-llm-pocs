package dev.nameless.poc.sqs.service;

import dev.nameless.poc.sqs.dto.QueueAttributesDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class QueuePolicyService {

    private final SqsClient sqsClient;

    public QueuePolicyService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void setQueuePolicy(String queueUrl, String policyJson) {
        sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(Map.of(QueueAttributeName.POLICY, policyJson))
                .build());
    }

    public Map<String, String> getQueuePolicy(String queueUrl) {
        GetQueueAttributesResponse response = sqsClient.getQueueAttributes(
                GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(QueueAttributeName.POLICY)
                        .build());
        String policy = response.attributesAsStrings()
                .getOrDefault(QueueAttributeName.POLICY.toString(), "none");
        return Map.of("policy", policy);
    }

    public void removeQueuePolicy(String queueUrl) {
        sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(Map.of(QueueAttributeName.POLICY, ""))
                .build());
    }

    public void setAdvancedAttributes(String queueUrl, QueueAttributesDto attrs) {
        Map<QueueAttributeName, String> attributes = new HashMap<>();

        if (attrs.visibilityTimeout() != null) {
            attributes.put(QueueAttributeName.VISIBILITY_TIMEOUT, String.valueOf(attrs.visibilityTimeout()));
        }
        if (attrs.messageRetentionPeriod() != null) {
            attributes.put(QueueAttributeName.MESSAGE_RETENTION_PERIOD, String.valueOf(attrs.messageRetentionPeriod()));
        }
        if (attrs.maximumMessageSize() != null) {
            attributes.put(QueueAttributeName.MAXIMUM_MESSAGE_SIZE, String.valueOf(attrs.maximumMessageSize()));
        }
        if (attrs.receiveMessageWaitTimeSeconds() != null) {
            attributes.put(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, String.valueOf(attrs.receiveMessageWaitTimeSeconds()));
        }

        sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(attributes)
                .build());
    }

    public Map<String, String> getAdvancedAttributes(String queueUrl) {
        GetQueueAttributesResponse response = sqsClient.getQueueAttributes(
                GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(
                                QueueAttributeName.VISIBILITY_TIMEOUT,
                                QueueAttributeName.MESSAGE_RETENTION_PERIOD,
                                QueueAttributeName.MAXIMUM_MESSAGE_SIZE,
                                QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS,
                                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE,
                                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_DELAYED)
                        .build());
        return response.attributesAsStrings();
    }
}
