package dev.nameless.poc.eventdriven.service;

import dev.nameless.poc.eventdriven.dto.CreateRuleDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.*;

import java.util.List;
import java.util.Map;

@Service
public class RuleService {

    private final EventBridgeClient eventBridgeClient;

    public RuleService(EventBridgeClient eventBridgeClient) {
        this.eventBridgeClient = eventBridgeClient;
    }

    public Map<String, String> createRule(String busName, CreateRuleDto dto) {
        PutRuleRequest.Builder builder = PutRuleRequest.builder()
                .name(dto.ruleName())
                .eventBusName(busName)
                .state(dto.enabled() ? RuleState.ENABLED : RuleState.DISABLED);

        if (dto.description() != null) builder.description(dto.description());
        if (dto.eventPattern() != null) builder.eventPattern(dto.eventPattern());
        if (dto.scheduleExpression() != null) builder.scheduleExpression(dto.scheduleExpression());

        PutRuleResponse response = eventBridgeClient.putRule(builder.build());
        return Map.of("ruleArn", response.ruleArn());
    }

    public List<Map<String, String>> listRules(String busName) {
        ListRulesResponse response = eventBridgeClient.listRules(
                ListRulesRequest.builder().eventBusName(busName).build());
        return response.rules().stream()
                .map(r -> Map.of(
                        "name", r.name(),
                        "arn", r.arn(),
                        "state", r.stateAsString(),
                        "eventPattern", r.eventPattern() != null ? r.eventPattern() : "n/a"))
                .toList();
    }

    public Map<String, String> describeRule(String busName, String ruleName) {
        DescribeRuleResponse response = eventBridgeClient.describeRule(
                DescribeRuleRequest.builder()
                        .eventBusName(busName)
                        .name(ruleName)
                        .build());
        return Map.of(
                "name", response.name(),
                "arn", response.arn(),
                "state", response.stateAsString(),
                "eventPattern", response.eventPattern() != null ? response.eventPattern() : "n/a",
                "scheduleExpression", response.scheduleExpression() != null ? response.scheduleExpression() : "n/a");
    }

    public void enableRule(String busName, String ruleName) {
        eventBridgeClient.enableRule(EnableRuleRequest.builder()
                .eventBusName(busName).name(ruleName).build());
    }

    public void disableRule(String busName, String ruleName) {
        eventBridgeClient.disableRule(DisableRuleRequest.builder()
                .eventBusName(busName).name(ruleName).build());
    }

    public void deleteRule(String busName, String ruleName) {
        eventBridgeClient.removeTargets(RemoveTargetsRequest.builder()
                .eventBusName(busName)
                .rule(ruleName)
                .ids(listTargets(busName, ruleName).stream()
                        .map(t -> t.get("id"))
                        .toList())
                .build());

        eventBridgeClient.deleteRule(DeleteRuleRequest.builder()
                .eventBusName(busName).name(ruleName).build());
    }

    public Map<String, String> putSqsTarget(String busName, String ruleName,
                                              String targetId, String sqsArn) {
        eventBridgeClient.putTargets(PutTargetsRequest.builder()
                .eventBusName(busName)
                .rule(ruleName)
                .targets(Target.builder()
                        .id(targetId)
                        .arn(sqsArn)
                        .build())
                .build());
        return Map.of("targetId", targetId, "targetArn", sqsArn);
    }

    public Map<String, String> putLambdaTarget(String busName, String ruleName,
                                                String targetId, String lambdaArn) {
        eventBridgeClient.putTargets(PutTargetsRequest.builder()
                .eventBusName(busName)
                .rule(ruleName)
                .targets(Target.builder()
                        .id(targetId)
                        .arn(lambdaArn)
                        .build())
                .build());
        return Map.of("targetId", targetId, "targetArn", lambdaArn);
    }

    public List<Map<String, String>> listTargets(String busName, String ruleName) {
        ListTargetsByRuleResponse response = eventBridgeClient.listTargetsByRule(
                ListTargetsByRuleRequest.builder()
                        .eventBusName(busName)
                        .rule(ruleName)
                        .build());
        return response.targets().stream()
                .map(t -> Map.of("id", t.id(), "arn", t.arn()))
                .toList();
    }
}
