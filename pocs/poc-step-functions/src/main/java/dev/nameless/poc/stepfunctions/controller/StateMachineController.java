package dev.nameless.poc.stepfunctions.controller;

import dev.nameless.poc.stepfunctions.dto.CreateStateMachineDto;
import dev.nameless.poc.stepfunctions.service.StateMachineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stepfunctions/machines")
public class StateMachineController {

    private final StateMachineService service;

    public StateMachineController(StateMachineService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody CreateStateMachineDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/describe")
    public ResponseEntity<Map<String, String>> describe(@RequestParam String stateMachineArn) {
        return ResponseEntity.ok(service.describe(stateMachineArn));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> delete(@RequestParam String stateMachineArn) {
        return ResponseEntity.ok(service.delete(stateMachineArn));
    }
}
