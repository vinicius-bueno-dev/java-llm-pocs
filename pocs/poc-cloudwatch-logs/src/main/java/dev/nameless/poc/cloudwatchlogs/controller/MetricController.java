package dev.nameless.poc.cloudwatchlogs.controller;

import dev.nameless.poc.cloudwatchlogs.dto.PutMetricDto;
import dev.nameless.poc.cloudwatchlogs.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cloudwatch/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostMapping
    public ResponseEntity<Void> putMetricData(@RequestParam String namespace,
                                               @RequestParam String metricName,
                                               @RequestParam double value,
                                               @RequestParam String unit) {
        metricService.putMetricData(namespace, metricName, value, unit);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> putMetricDataBatch(@RequestParam String namespace,
                                                                   @RequestBody List<PutMetricDto> metrics) {
        return ResponseEntity.ok(metricService.putMetricDataBatch(namespace, metrics));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listMetrics(
            @RequestParam(required = false) String namespace) {
        return ResponseEntity.ok(metricService.listMetrics(namespace));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMetricStatistics(
            @RequestParam String namespace,
            @RequestParam String metricName,
            @RequestParam(defaultValue = "Average") String stat,
            @RequestParam(defaultValue = "60") int periodMinutes) {
        return ResponseEntity.ok(metricService.getMetricStatistics(namespace, metricName, stat, periodMinutes));
    }

    @PostMapping("/alarms")
    public ResponseEntity<Void> putMetricAlarm(@RequestParam String alarmName,
                                                @RequestParam String namespace,
                                                @RequestParam String metricName,
                                                @RequestParam double threshold,
                                                @RequestParam String comparisonOperator) {
        metricService.putMetricAlarm(alarmName, namespace, metricName, threshold, comparisonOperator);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/alarms")
    public ResponseEntity<List<Map<String, Object>>> describeAlarms() {
        return ResponseEntity.ok(metricService.describeAlarms());
    }

    @DeleteMapping("/alarms/{alarmName}")
    public ResponseEntity<Void> deleteAlarm(@PathVariable String alarmName) {
        metricService.deleteAlarm(alarmName);
        return ResponseEntity.noContent().build();
    }
}
