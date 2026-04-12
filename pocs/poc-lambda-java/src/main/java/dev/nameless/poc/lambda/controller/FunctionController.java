package dev.nameless.poc.lambda.controller;

import dev.nameless.poc.lambda.dto.CreateFunctionDto;
import dev.nameless.poc.lambda.service.FunctionManagementService;
import dev.nameless.poc.lambda.service.LambdaDeployService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lambda/functions")
public class FunctionController {

    private final FunctionManagementService managementService;
    private final LambdaDeployService deployService;

    public FunctionController(FunctionManagementService managementService,
                               LambdaDeployService deployService) {
        this.managementService = managementService;
        this.deployService = deployService;
    }

    @PostMapping("/create-echo")
    public ResponseEntity<Map<String, String>> createEchoFunction(
            @RequestParam String functionName) throws IOException {
        byte[] zip = deployService.createEchoHandlerZip();
        CreateFunctionDto dto = new CreateFunctionDto(
                functionName, "handler.handler", "Echo function for testing", null, null, null);
        return ResponseEntity.ok(managementService.createFunction(dto, zip));
    }

    @PostMapping("/create-s3-trigger")
    public ResponseEntity<Map<String, String>> createS3TriggerFunction(
            @RequestParam String functionName) throws IOException {
        byte[] zip = deployService.createS3TriggerHandlerZip();
        CreateFunctionDto dto = new CreateFunctionDto(
                functionName, "handler.handler", "S3 event trigger handler", null, null, null);
        return ResponseEntity.ok(managementService.createFunction(dto, zip));
    }

    @PostMapping("/create-sqs-trigger")
    public ResponseEntity<Map<String, String>> createSqsTriggerFunction(
            @RequestParam String functionName) throws IOException {
        byte[] zip = deployService.createSqsTriggerHandlerZip();
        CreateFunctionDto dto = new CreateFunctionDto(
                functionName, "handler.handler", "SQS event trigger handler", null, null, null);
        return ResponseEntity.ok(managementService.createFunction(dto, zip));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> createFromZip(
            @RequestParam String functionName,
            @RequestParam String handler,
            @RequestParam(required = false) String description,
            @RequestPart("file") MultipartFile file) throws IOException {
        CreateFunctionDto dto = new CreateFunctionDto(
                functionName, handler, description, null, null, null);
        return ResponseEntity.ok(managementService.createFunction(dto, file.getBytes()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listFunctions() {
        return ResponseEntity.ok(managementService.listFunctions());
    }

    @GetMapping("/{functionName}")
    public ResponseEntity<Map<String, String>> getFunctionConfiguration(
            @PathVariable String functionName) {
        return ResponseEntity.ok(managementService.getFunctionConfiguration(functionName));
    }

    @PutMapping("/{functionName}/configuration")
    public ResponseEntity<Map<String, String>> updateConfiguration(
            @PathVariable String functionName,
            @RequestParam(required = false) Integer timeout,
            @RequestParam(required = false) Integer memorySize,
            @RequestBody(required = false) Map<String, String> envVars) {
        return ResponseEntity.ok(managementService.updateFunctionConfiguration(
                functionName, timeout, memorySize, envVars));
    }

    @DeleteMapping("/{functionName}")
    public ResponseEntity<Void> deleteFunction(@PathVariable String functionName) {
        managementService.deleteFunction(functionName);
        return ResponseEntity.noContent().build();
    }
}
