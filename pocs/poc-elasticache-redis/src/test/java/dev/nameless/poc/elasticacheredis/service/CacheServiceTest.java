package dev.nameless.poc.elasticacheredis.service;

import dev.nameless.poc.elasticacheredis.AbstractRedisTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CacheServiceTest extends AbstractRedisTest {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldPutAndGetValue() {
        cacheService.put("name", "Redis");

        String value = cacheService.get("name");

        assertThat(value).isEqualTo("Redis");
    }

    @Test
    void shouldReturnNullForMissingKey() {
        String value = cacheService.get("nonexistent");

        assertThat(value).isNull();
    }

    @Test
    void shouldDeleteKey() {
        cacheService.put("toDelete", "value");

        Boolean deleted = cacheService.delete("toDelete");

        assertThat(deleted).isTrue();
        assertThat(cacheService.get("toDelete")).isNull();
    }

    @Test
    void shouldCheckKeyExists() {
        cacheService.put("existing", "value");

        assertThat(cacheService.exists("existing")).isTrue();
        assertThat(cacheService.exists("missing")).isFalse();
    }

    @Test
    void shouldSetValueWithTtl() throws InterruptedException {
        cacheService.setWithTtl("temp", "expires", 2);

        assertThat(cacheService.get("temp")).isEqualTo("expires");

        Thread.sleep(2500);

        assertThat(cacheService.get("temp")).isNull();
    }

    @Test
    void shouldGetKeysByPattern() {
        cacheService.put("user:1", "Alice");
        cacheService.put("user:2", "Bob");
        cacheService.put("order:1", "Pizza");

        Set<String> userKeys = cacheService.getKeys("user:*");

        assertThat(userKeys).containsExactlyInAnyOrder("user:1", "user:2");
    }
}
