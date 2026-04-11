package dev.nameless.poc.dynamodb.controller;

import dev.nameless.poc.dynamodb.dto.PageResult;
import dev.nameless.poc.dynamodb.dto.UserDto;
import dev.nameless.poc.dynamodb.service.UserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dynamodb/users/query")
public class UserQueryController {

    private final UserQueryService service;

    public UserQueryController(UserQueryService service) {
        this.service = service;
    }

    @GetMapping("/by-tenant")
    public ResponseEntity<PageResult<UserDto>> listByTenant(
            @RequestParam String tenantId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(service.listByTenant(tenantId, limit, pageToken));
    }

    @GetMapping("/by-email")
    public ResponseEntity<PageResult<UserDto>> findByEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(service.findByEmail(email, limit, pageToken));
    }

    @GetMapping("/by-created-at")
    public ResponseEntity<PageResult<UserDto>> listByCreatedAt(
            @RequestParam String tenantId,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok(service.listByCreatedAt(tenantId, ascending, limit, pageToken));
    }
}
