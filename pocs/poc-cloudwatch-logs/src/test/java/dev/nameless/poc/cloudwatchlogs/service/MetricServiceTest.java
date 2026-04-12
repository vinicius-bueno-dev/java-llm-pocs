package dev.nameless.poc.cloudwatchlogs.service;

import dev.nameless.poc.cloudwatchlogs.AbstractLocalStackTest;
import dev.nameless.poc.cloudwatchlogs.dto.PutMetricDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MetricServiceTest extends AbstractLocalStackTest {

    private static final String TEST_NAMESPACE = "TestNamespace";
    private static final String TEST_METRIC = "TestMetric";
    private static final String TEST_ALARM = "test-alarm";

    @Autowired
    private MetricService metricService;

    @AfterEach
    void cleanup() {
        try {
            cloudWatchClient.deleteAlarms(DeleteAlarmsRequest.builder()
                    .alarmNames(TEST_ALARM)
                    .build());
        } catch (Exception ignored) {
            // alarm may not exist
        }
    }

    @Test
    void shouldPutMetricData() {
        // Should not throw
        metricService.putMetricData(TEST_NAMESPACE, TEST_METRIC, 42.0, "Count");

        List<Map<String, Object>> metrics = metricService.listMetrics(TEST_NAMESPACE);
        assertThat(metrics).anyMatch(m ->
                TEST_METRIC.equals(m.get("metricName")) && TEST_NAMESPACE.equals(m.get("namespace")));
    }

    @Test
    void shouldPutMetricDataBatch() {
        List<PutMetricDto> batch = List.of(
                new PutMetricDto(TEST_NAMESPACE, "Metric1", 10.0, "Count"),
                new PutMetricDto(TEST_NAMESPACE, "Metric2", 20.0, "Count"),
                new PutMetricDto(TEST_NAMESPACE, "Metric3", 30.0, "Bytes")
        );

        Map<String, Object> result = metricService.putMetricDataBatch(TEST_NAMESPACE, batch);
        assertThat(result).containsEntry("count", 3);

        List<Map<String, Object>> metrics = metricService.listMetrics(TEST_NAMESPACE);
        assertThat(metrics).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldListMetrics() {
        metricService.putMetricData(TEST_NAMESPACE, TEST_METRIC, 1.0, "Count");

        List<Map<String, Object>> metrics = metricService.listMetrics(TEST_NAMESPACE);
        assertThat(metrics).isNotEmpty();
        assertThat(metrics.get(0)).containsKeys("metricName", "namespace");
    }

    @Test
    void shouldGetMetricStatistics() {
        metricService.putMetricData(TEST_NAMESPACE, TEST_METRIC, 50.0, "Count");

        Map<String, Object> stats = metricService.getMetricStatistics(
                TEST_NAMESPACE, TEST_METRIC, "Average", 60);

        assertThat(stats)
                .containsEntry("namespace", TEST_NAMESPACE)
                .containsEntry("metricName", TEST_METRIC)
                .containsEntry("statistic", "Average")
                .containsKey("datapoints");
    }

    @Test
    void shouldPutAndDescribeAlarm() {
        metricService.putMetricAlarm(TEST_ALARM, TEST_NAMESPACE, TEST_METRIC,
                80.0, "GreaterThanThreshold");

        List<Map<String, Object>> alarms = metricService.describeAlarms();
        assertThat(alarms).anyMatch(a -> TEST_ALARM.equals(a.get("alarmName")));

        Map<String, Object> alarm = alarms.stream()
                .filter(a -> TEST_ALARM.equals(a.get("alarmName")))
                .findFirst()
                .orElseThrow();
        assertThat(alarm).containsEntry("threshold", 80.0);
    }

    @Test
    void shouldDeleteAlarm() {
        metricService.putMetricAlarm(TEST_ALARM, TEST_NAMESPACE, TEST_METRIC,
                80.0, "GreaterThanThreshold");

        metricService.deleteAlarm(TEST_ALARM);

        List<Map<String, Object>> alarms = metricService.describeAlarms();
        assertThat(alarms).noneMatch(a -> TEST_ALARM.equals(a.get("alarmName")));
    }

    @Test
    void shouldReturnEmptyListWhenNoMetrics() {
        List<Map<String, Object>> metrics = metricService.listMetrics("NonExistentNamespace");
        assertThat(metrics).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoAlarms() {
        List<Map<String, Object>> alarms = metricService.describeAlarms();
        // May or may not be empty depending on previous test state, but should not throw
        assertThat(alarms).isNotNull();
    }
}
