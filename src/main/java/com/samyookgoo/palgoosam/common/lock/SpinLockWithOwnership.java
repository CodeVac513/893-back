package com.samyookgoo.palgoosam.common.lock;

import java.time.Duration;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpinLockWithOwnership {
    private final RedisTemplate<String, Object> redisTemplate;

    // Lua script: 소유자만 락 해제 가능
    private static final String UNLOCK_SCRIPT =
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('DEL', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    private final DefaultRedisScript<Long> unlockScript = new DefaultRedisScript<>(
            UNLOCK_SCRIPT, Long.class
    );

    /**
     * 락 획득
     */
    public boolean tryLock(String key, String value, long timeoutSeconds) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofSeconds(timeoutSeconds));

        return Boolean.TRUE.equals(result);
    }

    /**
     * 락 해제 (소유권 검증)
     */
    public boolean unlock(String key, String value) {
        Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(key),
                value
        );
        boolean unlocked = result != null && result == 1L;

        if (unlocked) {
            log.info("락 해제 성공: key={}, value={}", key, value);
        } else {
            log.info("락 해제 실패 (소유자 아님): key={}, value={}", key, value);
        }
        return unlocked;
    }
}
