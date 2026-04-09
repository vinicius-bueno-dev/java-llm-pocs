package dev.nameless.poc.sqs.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.Map;

@Service
public class QueueTagService {

    private final SqsClient sqsClient;

    public QueueTagService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void tagQueue(String queueUrl, Map<String, String> tags) {
        sqsClient.tagQueue(TagQueueRequest.builder()
                .queueUrl(queueUrl)
                .tags(tags)
                .build());
    }

    public Map<String, String> listQueueTags(String queueUrl) {
        return sqsClient.listQueueTags(ListQueueTagsRequest.builder()
                .queueUrl(queueUrl)
                .build()).tags();
    }

    public void untagQueue(String queueUrl, List<String> tagKeys) {
        sqsClient.untagQueue(UntagQueueRequest.builder()
                .queueUrl(queueUrl)
                .tagKeys(tagKeys)
                .build());
    }
}
