package com.example.banktransfer.global.progress;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class RedisProgressRecorder implements ProgressRecorder {
    private static final String KEY_PROGRESS = "progress:";
    private static final String KEY_DETAIL   = "progress:detail:";

    private final StringRedisTemplate redisTemplate;

    public RedisProgressRecorder(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryStart(String key) {
        String redisKey = KEY_PROGRESS + key;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, ProgressStatus.PROCESSING.name(), Duration.ofMinutes(5));

        return Boolean.TRUE.equals(success);
    }

    @Override
    public ProgressStatus getStatus(String progressKey) {
        Object status = redisTemplate.opsForHash().get(KEY_DETAIL + progressKey, "status");
        if (status == null) {
            return null;
        }
        return ProgressStatus.valueOf(status.toString());
    }

    @Override
    public void record(String progressKey, ProgressStatus status, String reason) {
        String key = KEY_DETAIL + progressKey;
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
        redisTemplate.delete(KEY_DETAIL + progressKey);
        redisTemplate.delete(KEY_PROGRESS + progressKey);
    }
}
