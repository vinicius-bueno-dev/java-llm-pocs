package dev.nameless.poc.lambda.controller;

import dev.nameless.poc.lambda.service.FunctionInvocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lambda/invoke")
public class InvocationController {

    private final FunctionInvocationService invocationService;

    public InvocationController(FunctionInvocationService invocationService) {
        this.invocationService = invocationService;
    }

    @PostMapping("/sync/{functionName}")
    public ResponseEntity<Map<String, Object>> invokeSync(
            @PathVariable String functionName,
            @RequestBody(required = false) String payload) {
        return ResponseEntity.ok(invocationService.invokeSync(functionName, payload));
    }

    @PostMapping("/async/{functionName}")
    public ResponseEntity<Map<String, Object>> invokeAsync(
            @PathVariable String functionName,
            @RequestBody(required = false) String payload) {
        return ResponseEntity.ok(invocationService.invokeAsync(functionName, payload));
    }

    @PostMapping("/dry-run/{functionName}")
    public ResponseEntity<Map<String, Object>> invokeDryRun(
            @PathVariable String functionName,
            @RequestBody(required = false) String payload) {
        return ResponseEntity.ok(invocationService.invokeDryRun(functionName, payload));
    }
}
