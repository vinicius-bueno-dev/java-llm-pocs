package dev.nameless.poc.kinesisstreaming.controller;

import dev.nameless.poc.kinesisstreaming.service.StreamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kinesis/streams")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    @PostMapping("/{streamName}")
    public ResponseEntity<Void> createStream(
            @PathVariable String streamName,
            @RequestParam(defaultValue = "1") int shardCount) {
        streamService.createStream(streamName, shardCount);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<String>> listStreams() {
        return ResponseEntity.ok(streamService.listStreams());
    }

    @GetMapping("/{streamName}")
    public ResponseEntity<Map<String, Object>> describeStream(@PathVariable String streamName) {
        return ResponseEntity.ok(streamService.describeStream(streamName));
    }

    @DeleteMapping("/{streamName}")
    public ResponseEntity<Void> deleteStream(@PathVariable String streamName) {
        streamService.deleteStream(streamName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{streamName}/shards")
    public ResponseEntity<List<Map<String, String>>> listShards(@PathVariable String streamName) {
        return ResponseEntity.ok(streamService.listShards(streamName));
    }
}
