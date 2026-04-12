package dev.nameless.poc.elasticacheredis.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Checks whether a client is allowed to make a request using a sliding-window counter.
     * Uses Redis INCR + EXPIRE to track request counts within a time window.
     *
     * @param clientId      unique client identifier
     * @param maxRequests   maximum allowed requests in the window
     * @param windowSeconds size of the time window in seconds
     * @return true if the request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String clientId, int maxRequests, int windowSeconds) {
        String key = RATE_LIMIT_PREFIX + clientId;

        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount == null) {
            return false;
        }

        if (currentCount == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        return currentCount <= maxRequests;
    }
}
