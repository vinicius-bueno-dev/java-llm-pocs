package dev.nameless.poc.cloudwatchlogs.service;

import dev.nameless.poc.cloudwatchlogs.dto.PutLogEventDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.TagLogGroupRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogGroupService {

    private final CloudWatchLogsClient logsClient;

    public LogGroupService(CloudWatchLogsClient logsClient) {
        this.logsClient = logsClient;
    }

    public Map<String, Object> createLogGroup(String groupName, Map<String, String> tags) {
        logsClient.createLogGroup(CreateLogGroupRequest.builder()
                .logGroupName(groupName)
                .build());

        if (tags != null && !tags.isEmpty()) {
            logsClient.tagLogGroup(TagLogGroupRequest.builder()
                    .logGroupName(groupName)
                    .tags(tags)
                    .build());
        }

        return Map.of("logGroupName", groupName);
    }

    public Map<String, Object> createLogStream(String groupName, String streamName) {
        logsClient.createLogStream(CreateLogStreamRequest.builder()
                .logGroupName(groupName)
                .logStreamName(streamName)
                .build());

        return Map.of(
                "logGroupName", groupName,
                "logStreamName", streamName
        );
    }

    public List<Map<String, Object>> listLogGroups(String prefix) {
        DescribeLogGroupsRequest.Builder builder = DescribeLogGroupsRequest.builder();
        if (prefix != null && !prefix.isBlank()) {
            builder.logGroupNamePrefix(prefix);
        }

        return logsClient.describeLogGroups(builder.build())
                .logGroups()
                .stream()
                .map(this::toLogGroupMap)
                .toList();
    }

    public List<Map<String, Object>> listLogStreams(String groupName) {
        return logsClient.describeLogStreams(DescribeLogStreamsRequest.builder()
                        .logGroupName(groupName)
                        .build())
                .logStreams()
                .stream()
                .map(this::toLogStreamMap)
                .toList();
    }

    public void deleteLogGroup(String groupName) {
        logsClient.deleteLogGroup(DeleteLogGroupRequest.builder()
                .logGroupName(groupName)
                .build());
    }

    public void setRetentionPolicy(String groupName, int retentionDays) {
        logsClient.putRetentionPolicy(PutRetentionPolicyRequest.builder()
                .logGroupName(groupName)
                .retentionInDays(retentionDays)
                .build());
    }

    public Map<String, Object> putLogEvents(String groupName, String streamName, List<PutLogEventDto> events) {
        List<InputLogEvent> inputEvents = events.stream()
                .map(e -> InputLogEvent.builder()
                        .message(e.message())
                        .timestamp(e.effectiveTimestamp())
                        .build())
                .toList();

        PutLogEventsResponse response = logsClient.putLogEvents(PutLogEventsRequest.builder()
                .logGroupName(groupName)
                .logStreamName(streamName)
                .logEvents(inputEvents)
                .build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nextSequenceToken", response.nextSequenceToken());
        return result;
    }

    public List<Map<String, Object>> getLogEvents(String groupName, String streamName, int limit) {
        GetLogEventsResponse response = logsClient.getLogEvents(GetLogEventsRequest.builder()
                .logGroupName(groupName)
                .logStreamName(streamName)
                .limit(limit)
                .startFromHead(true)
                .build());

        return response.events().stream()
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("timestamp", e.timestamp());
                    map.put("message", e.message());
                    return map;
                })
                .toList();
    }

    private Map<String, Object> toLogGroupMap(LogGroup lg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("logGroupName", lg.logGroupName());
        map.put("arn", lg.arn());
        map.put("storedBytes", lg.storedBytes());
        return map;
    }

    private Map<String, Object> toLogStreamMap(LogStream ls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("logStreamName", ls.logStreamName());
        map.put("creationTime", ls.creationTime());
        return map;
    }
}
