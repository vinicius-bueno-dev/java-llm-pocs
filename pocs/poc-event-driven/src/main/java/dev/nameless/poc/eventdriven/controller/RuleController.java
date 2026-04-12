package dev.nameless.poc.eventdriven.controller;

import dev.nameless.poc.eventdriven.dto.CreateRuleDto;
import dev.nameless.poc.eventdriven.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/rules")
public class RuleController {

    private final RuleService service;

    public RuleController(RuleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createRule(
            @RequestParam String busName,
            @RequestBody CreateRuleDto dto) {
        return ResponseEntity.ok(service.createRule(busName, dto));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listRules(@RequestParam String busName) {
        return ResponseEntity.ok(service.listRules(busName));
    }

    @GetMapping("/{ruleName}")
    public ResponseEntity<Map<String, String>> describeRule(
            @RequestParam String busName,
            @PathVariable String ruleName) {
        return ResponseEntity.ok(service.describeRule(busName, ruleName));
    }

    @PutMapping("/{ruleName}/enable")
    public ResponseEntity<Void> enableRule(
            @RequestParam String busName,
            @PathVariable String ruleName) {
        service.enableRule(busName, ruleName);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{ruleName}/disable")
    public ResponseEntity<Void> disableRule(
            @RequestParam String busName,
            @PathVariable String ruleName) {
        service.disableRule(busName, ruleName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{ruleName}")
    public ResponseEntity<Void> deleteRule(
            @RequestParam String busName,
            @PathVariable String ruleName) {
        service.deleteRule(busName, ruleName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ruleName}/targets/sqs")
    public ResponseEntity<Map<String, String>> putSqsTarget(
            @RequestParam String busName,
            @PathVariable String ruleName,
            @RequestParam String targetId,
            @RequestParam String sqsArn) {
        return ResponseEntity.ok(service.putSqsTarget(busName, ruleName, targetId, sqsArn));
    }

    @PostMapping("/{ruleName}/targets/lambda")
    public ResponseEntity<Map<String, String>> putLambdaTarget(
            @RequestParam String busName,
            @PathVariable String ruleName,
            @RequestParam String targetId,
            @RequestParam String lambdaArn) {
        return ResponseEntity.ok(service.putLambdaTarget(busName, ruleName, targetId, lambdaArn));
    }

    @GetMapping("/{ruleName}/targets")
    public ResponseEntity<List<Map<String, String>>> listTargets(
            @RequestParam String busName,
            @PathVariable String ruleName) {
        return ResponseEntity.ok(service.listTargets(busName, ruleName));
    }
}
