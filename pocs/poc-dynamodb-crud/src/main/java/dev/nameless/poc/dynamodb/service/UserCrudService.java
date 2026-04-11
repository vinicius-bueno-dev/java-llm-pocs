package dev.nameless.poc.dynamodb.service;

import dev.nameless.poc.dynamodb.dto.CreateUserDto;
import dev.nameless.poc.dynamodb.dto.UpdateUserDto;
import dev.nameless.poc.dynamodb.dto.UserDto;
import dev.nameless.poc.dynamodb.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Orquestra os casos de uso CRUD basicos delegando ao {@link UserRepository}.
 * Nao mantem estado; serve como camada de aplicacao que aplica regras simples
 * (ex.: preencher {@code createdAt}) antes de delegar a persistencia.
 */
@Service
public class UserCrudService {

    private final UserRepository repository;

    public UserCrudService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDto create(CreateUserDto dto) {
        UserDto user = new UserDto(
                dto.tenantId(),
                dto.userId(),
                dto.email(),
                dto.name(),
                Instant.now().toString(),
                dto.expiresAt(),
                null);
        return repository.create(user);
    }

    public Optional<UserDto> get(String tenantId, String userId) {
        return repository.findByTenantAndUserId(tenantId, userId);
    }

    public UserDto updateName(String tenantId, String userId, UpdateUserDto dto) {
        if (dto.expectedVersion() == null) {
            throw new IllegalArgumentException("expectedVersion e obrigatorio para optimistic locking");
        }
        return repository.updateName(tenantId, userId, dto.name(), dto.expectedVersion());
    }

    public void delete(String tenantId, String userId) {
        repository.delete(tenantId, userId);
    }
}
