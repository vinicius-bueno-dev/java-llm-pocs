package dev.nameless.poc.dynamodb.service;

import dev.nameless.poc.dynamodb.AbstractLocalStackTest;
import dev.nameless.poc.dynamodb.dto.CreateUserDto;
import dev.nameless.poc.dynamodb.dto.PageResult;
import dev.nameless.poc.dynamodb.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class UserQueryServiceTest extends AbstractLocalStackTest {

    @Autowired
    private UserCrudService crudService;

    @Autowired
    private UserQueryService queryService;

    @BeforeEach
    void seed() {
        crudService.create(new CreateUserDto("acme", "u1", "u1@example.com", "User1", null));
        crudService.create(new CreateUserDto("acme", "u2", "u2@example.com", "User2", null));
        crudService.create(new CreateUserDto("acme", "u3", "u3@example.com", "User3", null));
        crudService.create(new CreateUserDto("globex", "u1", "g1@example.com", "GlobexUser1", null));
    }

    @Test
    void shouldListUsersByTenant() {
        PageResult<UserDto> page = queryService.listByTenant("acme", 10, null);
        assertThat(page.count()).isEqualTo(3);
        assertThat(page.items()).allMatch(u -> u.tenantId().equals("acme"));
    }

    @Test
    void shouldPaginateUsersByTenant() {
        PageResult<UserDto> first = queryService.listByTenant("acme", 2, null);
        assertThat(first.count()).isEqualTo(2);
        assertThat(first.nextToken()).isNotBlank();

        PageResult<UserDto> second = queryService.listByTenant("acme", 2, first.nextToken());
        assertThat(second.count()).isEqualTo(1);
        assertThat(second.nextToken()).isNull();
    }

    @Test
    void shouldFindUserByEmailUsingGsi() {
        PageResult<UserDto> page = queryService.findByEmail("u2@example.com", 10, null);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).userId()).isEqualTo("u2");
        assertThat(page.items().get(0).tenantId()).isEqualTo("acme");
    }

    @Test
    void shouldListByCreatedAtUsingLsi() {
        PageResult<UserDto> asc = queryService.listByCreatedAt("acme", true, 10, null);
        PageResult<UserDto> desc = queryService.listByCreatedAt("acme", false, 10, null);

        assertThat(asc.count()).isEqualTo(3);
        assertThat(desc.count()).isEqualTo(3);
        // ordem inversa
        assertThat(asc.items().get(0).userId())
                .isEqualTo(desc.items().get(2).userId());
    }
}
