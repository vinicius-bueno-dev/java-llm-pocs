package dev.nameless.poc.sns.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.List;
import java.util.Map;

@Service
public class TopicService {

    private final SnsClient snsClient;

    public TopicService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public Map<String, String> createTopic(String name, Map<String, String> tags) {
        CreateTopicRequest.Builder builder = CreateTopicRequest.builder().name(name);

        if (tags != null && !tags.isEmpty()) {
            List<Tag> snsTags = tags.entrySet().stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .toList();
            builder.tags(snsTags);
        }

        CreateTopicResponse response = snsClient.createTopic(builder.build());
        return Map.of("topicArn", response.topicArn());
    }

    public Map<String, String> createFifoTopic(String name, boolean contentBasedDedup) {
        String fifoName = name.endsWith(".fifo") ? name : name + ".fifo";

        CreateTopicResponse response = snsClient.createTopic(CreateTopicRequest.builder()
                .name(fifoName)
                .attributes(Map.of(
                        "FifoTopic", "true",
                        "ContentBasedDeduplication", String.valueOf(contentBasedDedup)))
                .build());

        return Map.of("topicArn", response.topicArn());
    }

    public List<Map<String, String>> listTopics() {
        ListTopicsResponse response = snsClient.listTopics();
        return response.topics().stream()
                .map(t -> Map.of("topicArn", t.topicArn()))
                .toList();
    }

    public Map<String, String> getTopicAttributes(String topicArn) {
        GetTopicAttributesResponse response = snsClient.getTopicAttributes(
                GetTopicAttributesRequest.builder().topicArn(topicArn).build());
        return response.attributes();
    }

    public void setTopicAttribute(String topicArn, String attributeName, String attributeValue) {
        snsClient.setTopicAttributes(SetTopicAttributesRequest.builder()
                .topicArn(topicArn)
                .attributeName(attributeName)
                .attributeValue(attributeValue)
                .build());
    }

    public void deleteTopic(String topicArn) {
        snsClient.deleteTopic(DeleteTopicRequest.builder().topicArn(topicArn).build());
    }

    public void tagTopic(String topicArn, Map<String, String> tags) {
        List<Tag> snsTags = tags.entrySet().stream()
                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                .toList();
        snsClient.tagResource(TagResourceRequest.builder()
                .resourceArn(topicArn)
                .tags(snsTags)
                .build());
    }

    public Map<String, String> listTopicTags(String topicArn) {
        ListTagsForResourceResponse response = snsClient.listTagsForResource(
                ListTagsForResourceRequest.builder().resourceArn(topicArn).build());
        return response.tags().stream()
                .collect(java.util.stream.Collectors.toMap(Tag::key, Tag::value));
    }
}
