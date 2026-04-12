package dev.nameless.poc.cloudwatchlogs.service;

import dev.nameless.poc.cloudwatchlogs.AbstractLocalStackTest;
import dev.nameless.poc.cloudwatchlogs.dto.PutLogEventDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogGroupServiceTest extends AbstractLocalStackTest {

    private static final String TEST_GROUP = "/test/log-group-service";
    private static final String TEST_STREAM = "test-stream";

    @Autowired
    private LogGroupService logGroupService;

    @AfterEach
    void cleanup() {
        try {
            logsClient.deleteLogGroup(DeleteLogGroupRequest.builder()
                    .logGroupName(TEST_GROUP)
                    .build());
        } catch (ResourceNotFoundException ignored) {
            // group may not exist
        }
    }

    @Test
    void shouldCreateLogGroup() {
        Map<String, Object> result = logGroupService.createLogGroup(TEST_GROUP, Map.of("env", "test"));

        assertThat(result).containsEntry("logGroupName", TEST_GROUP);

        List<Map<String, Object>> groups = logGroupService.listLogGroups("/test/");
        assertThat(groups).anyMatch(g -> TEST_GROUP.equals(g.get("logGroupName")));
    }

    @Test
    void shouldCreateLogStream() {
        logGroupService.createLogGroup(TEST_GROUP, null);

        Map<String, Object> result = logGroupService.createLogStream(TEST_GROUP, TEST_STREAM);

        assertThat(result)
                .containsEntry("logGroupName", TEST_GROUP)
                .containsEntry("logStreamName", TEST_STREAM);

        List<Map<String, Object>> streams = logGroupService.listLogStreams(TEST_GROUP);
        assertThat(streams).anyMatch(s -> TEST_STREAM.equals(s.get("logStreamName")));
    }

    @Test
    void shouldDeleteLogGroup() {
        logGroupService.createLogGroup(TEST_GROUP, null);
        logGroupService.deleteLogGroup(TEST_GROUP);

        List<Map<String, Object>> groups = logGroupService.listLogGroups(TEST_GROUP);
        assertThat(groups).noneMatch(g -> TEST_GROUP.equals(g.get("logGroupName")));
    }

    @Test
    void shouldSetRetentionPolicy() {
        logGroupService.createLogGroup(TEST_GROUP, null);

        // Should not throw
        logGroupService.setRetentionPolicy(TEST_GROUP, 7);

        List<Map<String, Object>> groups = logGroupService.listLogGroups(TEST_GROUP);
        assertThat(groups).isNotEmpty();
    }

    @Test
    void shouldPutAndGetLogEvents() {
        logGroupService.createLogGroup(TEST_GROUP, null);
        logGroupService.createLogStream(TEST_GROUP, TEST_STREAM);

        List<PutLogEventDto> events = List.of(
                new PutLogEventDto("First log message", System.currentTimeMillis()),
                new PutLogEventDto("Second log message", System.currentTimeMillis() + 1)
        );

        Map<String, Object> putResult = logGroupService.putLogEvents(TEST_GROUP, TEST_STREAM, events);
        assertThat(putResult).containsKey("nextSequenceToken");

        List<Map<String, Object>> logEvents = logGroupService.getLogEvents(TEST_GROUP, TEST_STREAM, 10);
        assertThat(logEvents).hasSize(2);
        assertThat(logEvents.get(0)).containsKey("timestamp");
        assertThat(logEvents.get(0)).containsKey("message");
        assertThat(logEvents.get(0).get("message")).isEqualTo("First log message");
        assertThat(logEvents.get(1).get("message")).isEqualTo("Second log message");
    }

    @Test
    void shouldReturnEmptyWhenNoLogEvents() {
        logGroupService.createLogGroup(TEST_GROUP, null);
        logGroupService.createLogStream(TEST_GROUP, TEST_STREAM);

        List<Map<String, Object>> events = logGroupService.getLogEvents(TEST_GROUP, TEST_STREAM, 10);
        assertThat(events).isEmpty();
    }

    @Test
    void shouldFailWhenDeletingNonExistentGroup() {
        assertThatThrownBy(() -> logGroupService.deleteLogGroup("/non-existent/group"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
