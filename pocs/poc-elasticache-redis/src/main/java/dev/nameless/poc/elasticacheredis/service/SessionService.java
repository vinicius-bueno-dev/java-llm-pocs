package dev.nameless.poc.elasticacheredis.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final long DEFAULT_SESSION_TTL_SECONDS = 1800; // 30 minutes

    private final StringRedisTemplate redisTemplate;

    public SessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Creates a new session with the given attributes stored as a Redis hash.
     *
     * @param attributes session attributes (key-value pairs)
     * @return the generated session ID
     */
    public String createSession(Map<String, String> attributes) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_PREFIX + sessionId;

        redisTemplate.opsForHash().putAll(key, attributes);
        redisTemplate.expire(key, DEFAULT_SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        return sessionId;
    }

    /**
     * Retrieves all attributes for a given session.
     *
     * @param sessionId the session ID
     * @return map of session attributes, or empty map if session does not exist
     */
    public Map<Object, Object> getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Deletes a session.
     *
     * @param sessionId the session ID
     * @return true if the session was deleted
     */
    public Boolean deleteSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return redisTemplate.delete(key);
    }

    /**
     * Extends the TTL of an existing session.
     *
     * @param sessionId      the session ID
     * @param extraSeconds   additional seconds to add to the session TTL
     * @return true if the timeout was set successfully
     */
    public Boolean extendSession(String sessionId, long extraSeconds) {
        String key = SESSION_PREFIX + sessionId;
        return redisTemplate.expire(key, extraSeconds, TimeUnit.SECONDS);
    }
}
