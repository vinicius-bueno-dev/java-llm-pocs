package dev.nameless.poc.stepfunctions.service;

import dev.nameless.poc.stepfunctions.dto.CreateStateMachineDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.CreateStateMachineRequest;
import software.amazon.awssdk.services.sfn.model.CreateStateMachineResponse;
import software.amazon.awssdk.services.sfn.model.DeleteStateMachineRequest;
import software.amazon.awssdk.services.sfn.model.DescribeStateMachineRequest;
import software.amazon.awssdk.services.sfn.model.DescribeStateMachineResponse;
import software.amazon.awssdk.services.sfn.model.ListStateMachinesRequest;
import software.amazon.awssdk.services.sfn.model.StateMachineListItem;
import software.amazon.awssdk.services.sfn.model.StateMachineType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servico para gerenciamento de state machines no AWS Step Functions.
 * Expoe operacoes de CRUD sobre state machines.
 */
@Service
public class StateMachineService {

    private final SfnClient sfnClient;

    public StateMachineService(SfnClient sfnClient) {
        this.sfnClient = sfnClient;
    }

    /**
     * Cria uma nova state machine com a definicao ASL fornecida.
     */
    public Map<String, String> create(CreateStateMachineDto dto) {
        CreateStateMachineResponse response = sfnClient.createStateMachine(
                CreateStateMachineRequest.builder()
                        .name(dto.name())
                        .definition(dto.definition())
                        .roleArn(dto.roleArn())
                        .type(StateMachineType.STANDARD)
                        .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("stateMachineArn", response.stateMachineArn());
        result.put("creationDate", response.creationDate().toString());
        return result;
    }

    /**
     * Lista todas as state machines.
     */
    public List<Map<String, String>> list() {
        List<StateMachineListItem> items = sfnClient.listStateMachines(
                ListStateMachinesRequest.builder().build()).stateMachines();

        return items.stream()
                .map(item -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("name", item.name());
                    map.put("stateMachineArn", item.stateMachineArn());
                    map.put("type", item.typeAsString());
                    map.put("creationDate", item.creationDate().toString());
                    return map;
                })
                .toList();
    }

    /**
     * Descreve uma state machine pelo ARN.
     */
    public Map<String, String> describe(String stateMachineArn) {
        DescribeStateMachineResponse response = sfnClient.describeStateMachine(
                DescribeStateMachineRequest.builder()
                        .stateMachineArn(stateMachineArn)
                        .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("name", response.name());
        result.put("stateMachineArn", response.stateMachineArn());
        result.put("status", response.statusAsString());
        result.put("definition", response.definition());
        result.put("roleArn", response.roleArn());
        result.put("type", response.typeAsString());
        result.put("creationDate", response.creationDate().toString());
        return result;
    }

    /**
     * Deleta uma state machine pelo ARN.
     */
    public Map<String, String> delete(String stateMachineArn) {
        sfnClient.deleteStateMachine(
                DeleteStateMachineRequest.builder()
                        .stateMachineArn(stateMachineArn)
                        .build());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("stateMachineArn", stateMachineArn);
        result.put("status", "DELETED");
        return result;
    }
}
