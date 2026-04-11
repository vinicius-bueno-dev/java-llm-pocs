package dev.nameless.poc.dynamodb.controller;

import dev.nameless.poc.dynamodb.dto.CreateUserDto;
import dev.nameless.poc.dynamodb.dto.UpdateUserDto;
import dev.nameless.poc.dynamodb.dto.UserDto;
import dev.nameless.poc.dynamodb.service.UserCrudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dynamodb/users")
public class UserCrudController {

    private final UserCrudService service;

    public UserCrudController(UserCrudService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody CreateUserDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{tenantId}/{userId}")
    public ResponseEntity<UserDto> get(@PathVariable String tenantId, @PathVariable String userId) {
        return service.get(tenantId, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{tenantId}/{userId}")
    public ResponseEntity<UserDto> update(
            @PathVariable String tenantId,
            @PathVariable String userId,
            @RequestBody UpdateUserDto dto) {
        return ResponseEntity.ok(service.updateName(tenantId, userId, dto));
    }

    @DeleteMapping("/{tenantId}/{userId}")
    public ResponseEntity<Void> delete(@PathVariable String tenantId, @PathVariable String userId) {
        service.delete(tenantId, userId);
        return ResponseEntity.noContent().build();
    }
}
