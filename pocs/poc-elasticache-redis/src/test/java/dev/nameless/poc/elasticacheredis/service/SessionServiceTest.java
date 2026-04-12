package dev.nameless.poc.elasticacheredis.service;

import dev.nameless.poc.elasticacheredis.AbstractRedisTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SessionServiceTest extends AbstractRedisTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        Set<String> keys = redisTemplate.keys("session:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldCreateAndRetrieveSession() {
        Map<String, String> attributes = Map.of("user", "Alice", "role", "admin");

        String sessionId = sessionService.createSession(attributes);

        assertThat(sessionId).isNotNull().isNotBlank();

        Map<Object, Object> session = sessionService.getSession(sessionId);
        assertThat(session).containsEntry("user", "Alice");
        assertThat(session).containsEntry("role", "admin");
    }

    @Test
    void shouldReturnEmptyMapForNonExistentSession() {
        Map<Object, Object> session = sessionService.getSession("nonexistent-id");

        assertThat(session).isEmpty();
    }

    @Test
    void shouldDeleteSession() {
        String sessionId = sessionService.createSession(Map.of("key", "value"));

        Boolean deleted = sessionService.deleteSession(sessionId);

        assertThat(deleted).isTrue();
        assertThat(sessionService.getSession(sessionId)).isEmpty();
    }

    @Test
    void shouldExtendSessionTtl() {
        String sessionId = sessionService.createSession(Map.of("key", "value"));

        Boolean extended = sessionService.extendSession(sessionId, 3600);

        assertThat(extended).isTrue();

        Long ttl = redisTemplate.getExpire("session:" + sessionId);
        assertThat(ttl).isNotNull().isGreaterThan(1800L);
    }

    @Test
    void shouldExpireSessionAfterTtl() throws InterruptedException {
        String sessionId = sessionService.createSession(Map.of("temp", "data"));
        // Override TTL to 2 seconds for testing
        redisTemplate.expire("session:" + sessionId, 2, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(sessionService.getSession(sessionId)).isNotEmpty();

        Thread.sleep(2500);

        assertThat(sessionService.getSession(sessionId)).isEmpty();
    }
}
