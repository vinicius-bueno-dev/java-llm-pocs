package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.dto.LifecycleRuleDto;
import dev.nameless.poc.s3.service.LifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/lifecycle")
public class LifecycleController {

    private final LifecycleService service;

    public LifecycleController(LifecycleService service) {
        this.service = service;
    }

    @PutMapping("/rules")
    public ResponseEntity<Void> setLifecycleRules(
            @RequestParam String bucket, @RequestBody List<LifecycleRuleDto> rules) {
        service.setLifecycleRules(bucket, rules);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rules")
    public ResponseEntity<List<Map<String, Object>>> getLifecycleRules(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getLifecycleRules(bucket));
    }

    @DeleteMapping("/rules")
    public ResponseEntity<Void> deleteLifecycleRules(@RequestParam String bucket) {
        service.deleteLifecycleRules(bucket);
        return ResponseEntity.noContent().build();
    }
}
