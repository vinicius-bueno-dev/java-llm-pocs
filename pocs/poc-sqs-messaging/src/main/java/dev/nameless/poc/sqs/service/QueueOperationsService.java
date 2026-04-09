package dev.nameless.poc.sqs.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.Map;

@Service
public class QueueOperationsService {

    private final SqsClient sqsClient;

    public QueueOperationsService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public String createStandardQueue(String queueName, Map<String, String> attributes) {
        CreateQueueRequest.Builder builder = CreateQueueRequest.builder().queueName(queueName);
        if (attributes != null && !attributes.isEmpty()) {
            builder.attributesWithStrings(attributes);
        }
        return sqsClient.createQueue(builder.build()).queueUrl();
    }

    public String createFifoQueue(String queueName, boolean contentBasedDeduplication) {
        return sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName(queueName)
                .attributes(Map.of(
                        QueueAttributeName.FIFO_QUEUE, "true",
                        QueueAttributeName.CONTENT_BASED_DEDUPLICATION, String.valueOf(contentBasedDeduplication)))
                .build()).queueUrl();
    }

    public List<String> listQueues(String prefix) {
        ListQueuesRequest.Builder builder = ListQueuesRequest.builder();
        if (prefix != null && !prefix.isBlank()) {
            builder.queueNamePrefix(prefix);
        }
        return sqsClient.listQueues(builder.build()).queueUrls();
    }

    public String getQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()).queueUrl();
    }

    public Map<String, String> getQueueAttributes(String queueUrl) {
        GetQueueAttributesResponse response = sqsClient.getQueueAttributes(
                GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNamesWithStrings("All")
                        .build());
        return response.attributesAsStrings();
    }

    public void setQueueAttributes(String queueUrl, Map<String, String> attributes) {
        sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributesWithStrings(attributes)
                .build());
    }

    public void deleteQueue(String queueUrl) {
        sqsClient.deleteQueue(DeleteQueueRequest.builder()
                .queueUrl(queueUrl)
                .build());
    }

    public void purgeQueue(String queueUrl) {
        sqsClient.purgeQueue(PurgeQueueRequest.builder()
                .queueUrl(queueUrl)
                .build());
    }
}
