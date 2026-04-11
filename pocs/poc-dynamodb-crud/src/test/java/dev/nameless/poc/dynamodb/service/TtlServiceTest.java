package dev.nameless.poc.dynamodb.service;

import dev.nameless.poc.dynamodb.AbstractLocalStackTest;
import dev.nameless.poc.dynamodb.dto.CreateUserDto;
import dev.nameless.poc.dynamodb.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TtlServiceTest extends AbstractLocalStackTest {

    @Autowired
    private TtlService ttlService;

    @Autowired
    private UserCrudService crudService;

    @Test
    void shouldDescribeTtlConfiguration() {
        String status = ttlService.describeTtl();
        assertThat(status).isIn("ENABLED", "ENABLING");
    }

    @Test
    void shouldSetExpiresAtAttribute() {
        crudService.create(new CreateUserDto("acme", "ttl-user", "ttl@example.com", "TTL", null));

        long expected = ttlService.setExpiresInSeconds("acme", "ttl-user", 3600);
        assertThat(expected).isGreaterThan(Instant.now().getEpochSecond());

        Optional<UserDto> updated = crudService.get("acme", "ttl-user");
        assertThat(updated).isPresent();
        assertThat(updated.get().expiresAt()).isEqualTo(expected);
    }
}
