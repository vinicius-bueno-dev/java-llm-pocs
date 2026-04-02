package dev.nameless.poc.s3.controller;

import dev.nameless.poc.s3.service.WebsiteHostingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/s3/website")
public class WebsiteHostingController {

    private final WebsiteHostingService service;

    public WebsiteHostingController(WebsiteHostingService service) {
        this.service = service;
    }

    @PutMapping("/configure")
    public ResponseEntity<Void> configureWebsiteHosting(
            @RequestParam String bucket,
            @RequestParam(defaultValue = "index.html") String indexDoc,
            @RequestParam(defaultValue = "error.html") String errorDoc) {
        service.configureWebsiteHosting(bucket, indexDoc, errorDoc);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getWebsiteConfiguration(@RequestParam String bucket) {
        return ResponseEntity.ok(service.getWebsiteConfiguration(bucket));
    }

    @DeleteMapping("/config")
    public ResponseEntity<Void> deleteWebsiteConfiguration(@RequestParam String bucket) {
        service.deleteWebsiteConfiguration(bucket);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deploy")
    public ResponseEntity<Map<String, String>> uploadWebsiteFiles(@RequestParam String bucket) {
        service.uploadWebsiteFiles(bucket);
        return ResponseEntity.ok(Map.of("status", "deployed", "files", "index.html, error.html"));
    }
}
