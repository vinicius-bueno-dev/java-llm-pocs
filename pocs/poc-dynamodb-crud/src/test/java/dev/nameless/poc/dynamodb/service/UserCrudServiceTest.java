package dev.nameless.poc.dynamodb.service;

import dev.nameless.poc.dynamodb.AbstractLocalStackTest;
import dev.nameless.poc.dynamodb.dto.CreateUserDto;
import dev.nameless.poc.dynamodb.dto.UpdateUserDto;
import dev.nameless.poc.dynamodb.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCrudServiceTest extends AbstractLocalStackTest {

    @Autowired
    private UserCrudService service;

    @Test
    void shouldCreateAndReadUser() {
        CreateUserDto dto = new CreateUserDto("acme", "alice", "alice@example.com", "Alice", null);
        UserDto created = service.create(dto);

        assertThat(created.version()).isEqualTo(1L);
        assertThat(created.createdAt()).isNotBlank();

        Optional<UserDto> found = service.get("acme", "alice");
        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo("alice@example.com");
        assertThat(found.get().name()).isEqualTo("Alice");
    }

    @Test
    void shouldReturnEmptyWhenUserMissing() {
        assertThat(service.get("acme", "missing")).isEmpty();
    }

    @Test
    void shouldUpdateNameWithOptimisticLocking() {
        service.create(new CreateUserDto("acme", "bob", "bob@example.com", "Bob", null));

        UserDto updated = service.updateName("acme", "bob",
                new UpdateUserDto("Bobby", null, 1L));

        assertThat(updated.name()).isEqualTo("Bobby");
        assertThat(updated.version()).isEqualTo(2L);
    }

    @Test
    void shouldRejectUpdateWhenVersionDoesNotMatch() {
        service.create(new CreateUserDto("acme", "carol", "carol@example.com", "Carol", null));

        assertThatThrownBy(() -> service.updateName("acme", "carol",
                new UpdateUserDto("CarolX", null, 99L)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldDeleteUser() {
        service.create(new CreateUserDto("acme", "dave", "dave@example.com", "Dave", null));
        service.delete("acme", "dave");
        assertThat(service.get("acme", "dave")).isEmpty();
    }
}
