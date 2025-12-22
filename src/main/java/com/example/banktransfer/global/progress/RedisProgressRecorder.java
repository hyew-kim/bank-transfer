package com.example.banktransfer.global.progress;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class RedisProgressRecorder implements ProgressRecorder {
    private static final String KEY_PREFIX = "progress:";

    private final StringRedisTemplate redisTemplate;

    public RedisProgressRecorder(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ProgressStatus getStatus(String progressKey) {
        Object status = redisTemplate.opsForHash().get(KEY_PREFIX + progressKey, "status");
        if (status == null) {
            return null;
        }
        return ProgressStatus.valueOf(status.toString());
    }

    @Override
    public void record(String progressKey, ProgressStatus status, String reason) {
        String key = KEY_PREFIX + progressKey;
        Map<String, String> values = new HashMap<>();
        values.put("status", status.name());
        values.put("updatedAt", Instant.now().toString());
        if (reason != null) {
            values.put("reason", reason);
        }

        redisTemplate.opsForHash().putAll(key, values);
    }

    @Override
    public void delete(String progressKey) {
        redisTemplate.delete(KEY_PREFIX + progressKey);
    }
}
