package dev.nameless.poc.sns.service;

import dev.nameless.poc.sns.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TopicServiceTest extends AbstractLocalStackTest {

    @Autowired
    private TopicService topicService;

    @Test
    void shouldCreateStandardTopic() {
        Map<String, String> result = topicService.createTopic("test-standard-topic", null);

        assertNotNull(result.get("topicArn"));
        assertTrue(result.get("topicArn").contains("test-standard-topic"));
    }

    @Test
    void shouldCreateTopicWithTags() {
        Map<String, String> tags = Map.of("Environment", "test", "POC", "sns");
        Map<String, String> result = topicService.createTopic("test-tagged-topic", tags);

        assertNotNull(result.get("topicArn"));

        Map<String, String> retrievedTags = topicService.listTopicTags(result.get("topicArn"));
        assertEquals("test", retrievedTags.get("Environment"));
        assertEquals("sns", retrievedTags.get("POC"));
    }

    @Test
    void shouldCreateFifoTopic() {
        Map<String, String> result = topicService.createFifoTopic("test-fifo-topic", true);

        assertNotNull(result.get("topicArn"));
        assertTrue(result.get("topicArn").contains("test-fifo-topic.fifo"));
    }

    @Test
    void shouldListTopics() {
        topicService.createTopic("test-list-topic-1", null);
        topicService.createTopic("test-list-topic-2", null);

        List<Map<String, String>> topics = topicService.listTopics();

        assertTrue(topics.size() >= 2);
    }

    @Test
    void shouldGetTopicAttributes() {
        Map<String, String> created = topicService.createTopic("test-attrs-topic", null);
        String topicArn = created.get("topicArn");

        Map<String, String> attributes = topicService.getTopicAttributes(topicArn);

        assertNotNull(attributes.get("TopicArn"));
        assertEquals(topicArn, attributes.get("TopicArn"));
    }

    @Test
    void shouldDeleteTopic() {
        Map<String, String> created = topicService.createTopic("test-delete-topic", null);
        String topicArn = created.get("topicArn");

        topicService.deleteTopic(topicArn);

        List<Map<String, String>> topics = topicService.listTopics();
        boolean found = topics.stream().anyMatch(t -> t.get("topicArn").equals(topicArn));
        assertFalse(found);
    }
}
