package com.pulsepass.notificationservice.infrastructure.redis;

import com.pulsepass.notificationservice.domain.Cache;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCache implements Cache {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;

    public RedisCache(StringRedisTemplate redisTemplate, @Value("${spring.application.name}") String serviceName) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = serviceName + ":";
    }

    @Override
    public boolean acquireLock(String key, Duration ttl) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(keyPrefix + key, "locked", ttl);
        return Boolean.TRUE.equals(locked);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(keyPrefix + key);
    }
}
