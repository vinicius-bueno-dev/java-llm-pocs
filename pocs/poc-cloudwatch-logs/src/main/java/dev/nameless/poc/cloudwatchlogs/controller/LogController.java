package dev.nameless.poc.cloudwatchlogs.controller;

import dev.nameless.poc.cloudwatchlogs.dto.PutLogEventDto;
import dev.nameless.poc.cloudwatchlogs.service.LogGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cloudwatch/logs")
public class LogController {

    private final LogGroupService logGroupService;

    public LogController(LogGroupService logGroupService) {
        this.logGroupService = logGroupService;
    }

    @PostMapping("/groups")
    public ResponseEntity<Map<String, Object>> createLogGroup(@RequestParam String groupName,
                                                               @RequestBody(required = false) Map<String, String> tags) {
        return ResponseEntity.ok(logGroupService.createLogGroup(groupName, tags));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Map<String, Object>>> listLogGroups(
            @RequestParam(required = false) String prefix) {
        return ResponseEntity.ok(logGroupService.listLogGroups(prefix));
    }

    @DeleteMapping("/groups/{groupName}")
    public ResponseEntity<Void> deleteLogGroup(@PathVariable String groupName) {
        logGroupService.deleteLogGroup(groupName);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/groups/{groupName}/retention")
    public ResponseEntity<Void> setRetentionPolicy(@PathVariable String groupName,
                                                     @RequestParam int retentionDays) {
        logGroupService.setRetentionPolicy(groupName, retentionDays);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/groups/{groupName}/streams")
    public ResponseEntity<Map<String, Object>> createLogStream(@PathVariable String groupName,
                                                                @RequestParam String streamName) {
        return ResponseEntity.ok(logGroupService.createLogStream(groupName, streamName));
    }

    @GetMapping("/groups/{groupName}/streams")
    public ResponseEntity<List<Map<String, Object>>> listLogStreams(@PathVariable String groupName) {
        return ResponseEntity.ok(logGroupService.listLogStreams(groupName));
    }

    @PostMapping("/groups/{groupName}/streams/{streamName}/events")
    public ResponseEntity<Map<String, Object>> putLogEvents(@PathVariable String groupName,
                                                             @PathVariable String streamName,
                                                             @RequestBody List<PutLogEventDto> events) {
        return ResponseEntity.ok(logGroupService.putLogEvents(groupName, streamName, events));
    }

    @GetMapping("/groups/{groupName}/streams/{streamName}/events")
    public ResponseEntity<List<Map<String, Object>>> getLogEvents(@PathVariable String groupName,
                                                                   @PathVariable String streamName,
                                                                   @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(logGroupService.getLogEvents(groupName, streamName, limit));
    }
}
