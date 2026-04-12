package dev.nameless.poc.elasticacheredis.service;

import dev.nameless.poc.elasticacheredis.AbstractRedisTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterServiceTest extends AbstractRedisTest {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        Set<String> keys = redisTemplate.keys("ratelimit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        boolean first = rateLimiterService.isAllowed("client-1", 3, 60);
        boolean second = rateLimiterService.isAllowed("client-1", 3, 60);
        boolean third = rateLimiterService.isAllowed("client-1", 3, 60);

        assertThat(first).isTrue();
        assertThat(second).isTrue();
        assertThat(third).isTrue();
    }

    @Test
    void shouldBlockRequestsExceedingLimit() {
        for (int i = 0; i < 3; i++) {
            rateLimiterService.isAllowed("client-2", 3, 60);
        }

        boolean fourth = rateLimiterService.isAllowed("client-2", 3, 60);

        assertThat(fourth).isFalse();
    }

    @Test
    void shouldTrackClientsIndependently() {
        rateLimiterService.isAllowed("client-a", 1, 60);
        boolean blockedA = rateLimiterService.isAllowed("client-a", 1, 60);
        boolean allowedB = rateLimiterService.isAllowed("client-b", 1, 60);

        assertThat(blockedA).isFalse();
        assertThat(allowedB).isTrue();
    }

    @Test
    void shouldResetAfterWindowExpires() throws InterruptedException {
        rateLimiterService.isAllowed("client-3", 1, 2);
        boolean blocked = rateLimiterService.isAllowed("client-3", 1, 2);
        assertThat(blocked).isFalse();

        Thread.sleep(2500);

        boolean allowedAgain = rateLimiterService.isAllowed("client-3", 1, 2);
        assertThat(allowedAgain).isTrue();
    }
}
