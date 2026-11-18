package com.pulsepass.notificationservice.unit.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.notificationservice.infrastructure.redis.RedisCache;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisCacheTest {

    private static final Duration TTL = Duration.ofSeconds(60);

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisCache cache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cache = new RedisCache(redisTemplate, "notification-service");
    }

    @Test
    void acquireLock_keyFree_returnsTrueAndSetsPrefixedKeyWithTtl() {
        when(valueOperations.setIfAbsent("notification-service:k", "locked", TTL)).thenReturn(true);

        assertThat(cache.acquireLock("k", TTL)).isTrue();

        verify(valueOperations).setIfAbsent("notification-service:k", "locked", TTL);
    }

    @Test
    void acquireLock_keyTaken_returnsFalse() {
        when(valueOperations.setIfAbsent("notification-service:k", "locked", TTL)).thenReturn(false);

        assertThat(cache.acquireLock("k", TTL)).isFalse();
    }

    @Test
    void acquireLock_nullReply_returnsFalse() {
        when(valueOperations.setIfAbsent("notification-service:k", "locked", TTL)).thenReturn(null);

        assertThat(cache.acquireLock("k", TTL)).isFalse();
    }

    @Test
    void delete_key_deletesPrefixedKey() {
        cache.delete("k");

        verify(redisTemplate).delete("notification-service:k");
    }
}
