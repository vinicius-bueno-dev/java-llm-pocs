package dev.nameless.poc.sqs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nameless.poc.sqs.dto.RedriveConfigDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeadLetterQueueService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeadLetterQueueService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void configureRedrivePolicy(String queueUrl, RedriveConfigDto config) {
        try {
            String redrivePolicy = objectMapper.writeValueAsString(Map.of(
                    "deadLetterTargetArn", config.deadLetterQueueArn(),
                    "maxReceiveCount", config.maxReceiveCount()));

            sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                    .queueUrl(queueUrl)
                    .attributes(Map.of(QueueAttributeName.REDRIVE_POLICY, redrivePolicy))
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize redrive policy", e);
        }
    }

    public Map<String, String> getRedrivePolicy(String queueUrl) {
        GetQueueAttributesResponse response = sqsClient.getQueueAttributes(
                GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(QueueAttributeName.REDRIVE_POLICY)
                        .build());

        String policy = response.attributesAsStrings()
                .getOrDefault(QueueAttributeName.REDRIVE_POLICY.toString(), "none");
        return Map.of("redrivePolicy", policy);
    }

    public List<Map<String, String>> listDlqMessages(String dlqUrl, int maxMessages) {
        ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(dlqUrl)
                .maxNumberOfMessages(maxMessages)
                .messageSystemAttributeNamesWithStrings("All")
                .build());

        return response.messages().stream()
                .map(msg -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("messageId", msg.messageId());
                    result.put("body", msg.body());
                    result.put("receiptHandle", msg.receiptHandle());
                    msg.attributesAsStrings().forEach((k, v) -> result.put("sys_" + k, v));
                    return result;
                })
                .toList();
    }

    public Map<String, String> startMessageMoveTask(String dlqArn, String destinationQueueArn) {
        StartMessageMoveTaskResponse response = sqsClient.startMessageMoveTask(
                StartMessageMoveTaskRequest.builder()
                        .sourceArn(dlqArn)
                        .destinationArn(destinationQueueArn)
                        .build());
        return Map.of("taskHandle", response.taskHandle());
    }

    public Map<String, Object> simulateFailure(String queueUrl, int receiveCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("receivesRequested", receiveCount);

        int actualReceives = 0;
        for (int i = 0; i < receiveCount; i++) {
            ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(1)
                    .visibilityTimeout(0)
                    .build());

            if (!response.messages().isEmpty()) {
                actualReceives++;
                result.put("lastMessageId", response.messages().getFirst().messageId());
            }
        }

        result.put("actualReceives", actualReceives);
        return result;
    }
}
