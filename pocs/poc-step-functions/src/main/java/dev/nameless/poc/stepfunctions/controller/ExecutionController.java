package dev.nameless.poc.stepfunctions.controller;

import dev.nameless.poc.stepfunctions.dto.StartExecutionDto;
import dev.nameless.poc.stepfunctions.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stepfunctions/executions")
public class ExecutionController {

    private final ExecutionService service;

    public ExecutionController(ExecutionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> start(@RequestBody StartExecutionDto dto) {
        return ResponseEntity.ok(service.startExecution(dto));
    }

    @GetMapping("/describe")
    public ResponseEntity<Map<String, String>> describe(@RequestParam String executionArn) {
        return ResponseEntity.ok(service.describeExecution(executionArn));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> list(@RequestParam String stateMachineArn) {
        return ResponseEntity.ok(service.listExecutions(stateMachineArn));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stop(
            @RequestParam String executionArn,
            @RequestParam(required = false) String cause,
            @RequestParam(required = false) String error) {
        return ResponseEntity.ok(service.stopExecution(executionArn, cause, error));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, String>>> history(@RequestParam String executionArn) {
        return ResponseEntity.ok(service.getExecutionHistory(executionArn));
    }
}
