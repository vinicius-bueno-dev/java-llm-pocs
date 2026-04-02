package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.dto.CorsRuleDto;
import dev.nameless.poc.s3.service.CorsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3/cors")
public class CorsController {

    private final CorsService service;

    public CorsController(CorsService service) {
        this.service = service;
    }

    @PutMapping("/config")
    public ResponseEntity<Void> setCorsConfiguration(
            @RequestParam String bucket, @RequestBody List<CorsRuleDto> rules) {
        service.setCorsConfiguration(bucket, rules);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config")
    public ResponseEntity<List<Map<String, Object>>> getCorsConfiguration(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getCorsConfiguration(bucket));
    }

    @DeleteMapping("/config")
    public ResponseEntity<Void> deleteCorsConfiguration(@RequestParam String bucket) {
        service.deleteCorsConfiguration(bucket);
        return ResponseEntity.noContent().build();
    }
}
