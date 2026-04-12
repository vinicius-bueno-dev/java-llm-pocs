package dev.nameless.poc.cloudwatchlogs.service;

import dev.nameless.poc.cloudwatchlogs.dto.PutMetricDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.ComparisonOperator;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricAlarm;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricService {

    private final CloudWatchClient cloudWatchClient;

    public MetricService(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public void putMetricData(String namespace, String metricName, double value, String unit) {
        MetricDatum datum = MetricDatum.builder()
                .metricName(metricName)
                .value(value)
                .unit(StandardUnit.fromValue(unit))
                .timestamp(Instant.now())
                .build();

        cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
                .namespace(namespace)
                .metricData(datum)
                .build());
    }

    public Map<String, Object> putMetricDataBatch(String namespace, List<PutMetricDto> metrics) {
        List<MetricDatum> data = metrics.stream()
                .map(m -> MetricDatum.builder()
                        .metricName(m.metricName())
                        .value(m.value())
                        .unit(StandardUnit.fromValue(m.unit()))
                        .timestamp(Instant.now())
                        .build())
                .toList();

        cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
                .namespace(namespace)
                .metricData(data)
                .build());

        return Map.of("count", data.size());
    }

    public List<Map<String, Object>> listMetrics(String namespace) {
        ListMetricsRequest.Builder builder = ListMetricsRequest.builder();
        if (namespace != null && !namespace.isBlank()) {
            builder.namespace(namespace);
        }

        return cloudWatchClient.listMetrics(builder.build())
                .metrics()
                .stream()
                .map(this::toMetricMap)
                .toList();
    }

    public Map<String, Object> getMetricStatistics(String namespace, String metricName,
                                                     String stat, int periodMinutes) {
        Instant end = Instant.now();
        Instant start = end.minusSeconds((long) periodMinutes * 60);

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(
                GetMetricStatisticsRequest.builder()
                        .namespace(namespace)
                        .metricName(metricName)
                        .statistics(Statistic.fromValue(stat))
                        .startTime(start)
                        .endTime(end)
                        .period(periodMinutes * 60)
                        .build());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("namespace", namespace);
        result.put("metricName", metricName);
        result.put("statistic", stat);
        result.put("datapoints", response.datapoints().stream()
                .map(dp -> {
                    Map<String, Object> dpMap = new LinkedHashMap<>();
                    dpMap.put("timestamp", dp.timestamp().toString());
                    dpMap.put("sum", dp.sum());
                    dpMap.put("average", dp.average());
                    dpMap.put("maximum", dp.maximum());
                    dpMap.put("minimum", dp.minimum());
                    dpMap.put("sampleCount", dp.sampleCount());
                    return dpMap;
                })
                .toList());
        return result;
    }

    public void putMetricAlarm(String alarmName, String namespace, String metricName,
                                double threshold, String comparisonOperator) {
        cloudWatchClient.putMetricAlarm(PutMetricAlarmRequest.builder()
                .alarmName(alarmName)
                .namespace(namespace)
                .metricName(metricName)
                .threshold(threshold)
                .comparisonOperator(ComparisonOperator.fromValue(comparisonOperator))
                .evaluationPeriods(1)
                .period(60)
                .statistic(Statistic.AVERAGE)
                .actionsEnabled(false)
                .build());
    }

    public List<Map<String, Object>> describeAlarms() {
        return cloudWatchClient.describeAlarms(DescribeAlarmsRequest.builder().build())
                .metricAlarms()
                .stream()
                .map(this::toAlarmMap)
                .toList();
    }

    public void deleteAlarm(String alarmName) {
        cloudWatchClient.deleteAlarms(DeleteAlarmsRequest.builder()
                .alarmNames(alarmName)
                .build());
    }

    private Map<String, Object> toMetricMap(Metric m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("metricName", m.metricName());
        map.put("namespace", m.namespace());
        return map;
    }

    private Map<String, Object> toAlarmMap(MetricAlarm a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("alarmName", a.alarmName());
        map.put("state", a.stateValueAsString());
        map.put("threshold", a.threshold());
        return map;
    }
}
