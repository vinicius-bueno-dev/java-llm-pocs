package dev.nameless.poc.elasticacheredis.controller;

import dev.nameless.poc.elasticacheredis.dto.CacheEntryDto;
import dev.nameless.poc.elasticacheredis.service.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/redis/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> put(@RequestBody CacheEntryDto entry) {
        if (entry.ttlSeconds() != null && entry.ttlSeconds() > 0) {
            cacheService.setWithTtl(entry.key(), entry.value(), entry.ttlSeconds());
        } else {
            cacheService.put(entry.key(), entry.value());
        }
        return ResponseEntity.ok(Map.of("status", "OK", "key", entry.key()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> get(@PathVariable String key) {
        String value = cacheService.get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String key) {
        Boolean deleted = cacheService.delete(key);
        return ResponseEntity.ok(Map.of("key", key, "deleted", deleted));
    }

    @GetMapping("/{key}/exists")
    public ResponseEntity<Map<String, Object>> exists(@PathVariable String key) {
        Boolean exists = cacheService.exists(key);
        return ResponseEntity.ok(Map.of("key", key, "exists", exists));
    }

    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getKeys(@RequestParam(defaultValue = "*") String pattern) {
        return ResponseEntity.ok(cacheService.getKeys(pattern));
    }
}
