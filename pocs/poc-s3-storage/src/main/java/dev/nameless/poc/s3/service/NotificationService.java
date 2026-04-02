package dev.nameless.poc.s3.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final S3Client s3Client;
    private final SqsClient sqsClient;

    public NotificationService(S3Client s3Client, SqsClient sqsClient) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
    }

    public void configureQueueNotification(String bucket, String queueArn,
                                           List<String> events, String filterPrefix) {
        QueueConfiguration queueConfig = QueueConfiguration.builder()
                .queueArn(queueArn)
                .eventsWithStrings(events)
                .filter(NotificationConfigurationFilter.builder()
                        .key(S3KeyFilter.builder()
                                .filterRules(FilterRule.builder()
                                        .name(FilterRuleName.PREFIX)
                                        .value(filterPrefix)
                                        .build())
                                .build())
                        .build())
                .build();

        s3Client.putBucketNotificationConfiguration(PutBucketNotificationConfigurationRequest.builder()
                .bucket(bucket)
                .notificationConfiguration(NotificationConfiguration.builder()
                        .queueConfigurations(queueConfig)
                        .build())
                .build());
    }

    public void configureTopicNotification(String bucket, String topicArn,
                                           List<String> events, String filterPrefix) {
        TopicConfiguration topicConfig = TopicConfiguration.builder()
                .topicArn(topicArn)
                .eventsWithStrings(events)
                .filter(NotificationConfigurationFilter.builder()
                        .key(S3KeyFilter.builder()
                                .filterRules(FilterRule.builder()
                                        .name(FilterRuleName.PREFIX)
                                        .value(filterPrefix)
                                        .build())
                                .build())
                        .build())
                .build();

        s3Client.putBucketNotificationConfiguration(PutBucketNotificationConfigurationRequest.builder()
                .bucket(bucket)
                .notificationConfiguration(NotificationConfiguration.builder()
                        .topicConfigurations(topicConfig)
                        .build())
                .build());
    }

    public Map<String, Object> getNotificationConfiguration(String bucket) {
        GetBucketNotificationConfigurationResponse response =
                s3Client.getBucketNotificationConfiguration(
                        GetBucketNotificationConfigurationRequest.builder()
                                .bucket(bucket).build());

        List<Map<String, Object>> queues = response.queueConfigurations().stream()
                .map(q -> Map.<String, Object>of(
                        "queueArn", q.queueArn(),
                        "events", q.eventsAsStrings()))
                .toList();

        List<Map<String, Object>> topics = response.topicConfigurations().stream()
                .map(t -> Map.<String, Object>of(
                        "topicArn", t.topicArn(),
                        "events", t.eventsAsStrings()))
                .toList();

        return Map.of("queueConfigurations", queues, "topicConfigurations", topics);
    }

    public void deleteNotificationConfiguration(String bucket) {
        s3Client.putBucketNotificationConfiguration(PutBucketNotificationConfigurationRequest.builder()
                .bucket(bucket)
                .notificationConfiguration(NotificationConfiguration.builder().build())
                .build());
    }

    public List<String> pollSqsMessages(String queueUrl, int maxMessages) {
        return sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(maxMessages)
                .waitTimeSeconds(5)
                .build())
                .messages().stream()
                .map(Message::body)
                .toList();
    }
}
