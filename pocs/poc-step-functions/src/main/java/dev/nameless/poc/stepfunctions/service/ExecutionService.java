package dev.nameless.poc.stepfunctions.service;

import dev.nameless.poc.stepfunctions.dto.StartExecutionDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.DescribeExecutionRequest;
import software.amazon.awssdk.services.sfn.model.DescribeExecutionResponse;
import software.amazon.awssdk.services.sfn.model.ExecutionListItem;
import software.amazon.awssdk.services.sfn.model.GetExecutionHistoryRequest;
import software.amazon.awssdk.services.sfn.model.GetExecutionHistoryResponse;
import software.amazon.awssdk.services.sfn.model.HistoryEvent;
import software.amazon.awssdk.services.sfn.model.ListExecutionsRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;
import software.amazon.awssdk.services.sfn.model.StopExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StopExecutionResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servico para gerenciamento de execucoes de state machines.
 * Permite iniciar, descrever, listar, parar execucoes e consultar historico.
 */
@Service
public class ExecutionService {

    private final SfnClient sfnClient;

    public ExecutionService(SfnClient sfnClient) {
        this.sfnClient = sfnClient;
    }

    /**
     * Inicia uma nova execucao de uma state machine.
     */
    public Map<String, String> startExecution(StartExecutionDto dto) {
        StartExecutionRequest.Builder requestBuilder = StartExecutionRequest.builder()
                .stateMachineArn(dto.stateMachineArn())
                .input(dto.input());

        if (dto.name() != null && !dto.name().isBlank()) {
            requestBuilder.name(dto.name());
        }

        StartExecutionResponse response = sfnClient.startExecution(requestBuilder.build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("executionArn", response.executionArn());
        result.put("startDate", response.startDate().toString());
        return result;
    }

    /**
     * Descreve uma execucao pelo ARN.
     */
    public Map<String, String> describeExecution(String executionArn) {
        DescribeExecutionResponse response = sfnClient.describeExecution(
                DescribeExecutionRequest.builder()
                        .executionArn(executionArn)
                        .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("executionArn", response.executionArn());
        result.put("stateMachineArn", response.stateMachineArn());
        result.put("status", response.statusAsString());
        result.put("startDate", response.startDate().toString());
        if (response.stopDate() != null) {
            result.put("stopDate", response.stopDate().toString());
        }
        result.put("input", response.input());
        if (response.output() != null) {
            result.put("output", response.output());
        }
        return result;
    }

    /**
     * Lista execucoes de uma state machine.
     */
    public List<Map<String, String>> listExecutions(String stateMachineArn) {
        List<ExecutionListItem> items = sfnClient.listExecutions(
                ListExecutionsRequest.builder()
                        .stateMachineArn(stateMachineArn)
                        .build()).executions();

        return items.stream()
                .map(item -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("executionArn", item.executionArn());
                    map.put("name", item.name());
                    map.put("status", item.statusAsString());
                    map.put("startDate", item.startDate().toString());
                    if (item.stopDate() != null) {
                        map.put("stopDate", item.stopDate().toString());
                    }
                    return map;
                })
                .toList();
    }

    /**
     * Para uma execucao em andamento.
     */
    public Map<String, String> stopExecution(String executionArn, String cause, String error) {
        StopExecutionRequest.Builder requestBuilder = StopExecutionRequest.builder()
                .executionArn(executionArn);

        if (cause != null) {
            requestBuilder.cause(cause);
        }
        if (error != null) {
            requestBuilder.error(error);
        }

        StopExecutionResponse response = sfnClient.stopExecution(requestBuilder.build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("executionArn", executionArn);
        result.put("stopDate", response.stopDate().toString());
        result.put("status", "ABORTED");
        return result;
    }

    /**
     * Retorna o historico de eventos de uma execucao.
     */
    public List<Map<String, String>> getExecutionHistory(String executionArn) {
        GetExecutionHistoryResponse response = sfnClient.getExecutionHistory(
                GetExecutionHistoryRequest.builder()
                        .executionArn(executionArn)
                        .build());

        return response.events().stream()
                .map(this::eventToMap)
                .toList();
    }

    private Map<String, String> eventToMap(HistoryEvent event) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("id", String.valueOf(event.id()));
        map.put("type", event.typeAsString());
        map.put("timestamp", event.timestamp().toString());
        if (event.previousEventId() != null) {
            map.put("previousEventId", String.valueOf(event.previousEventId()));
        }
        return map;
    }
}
