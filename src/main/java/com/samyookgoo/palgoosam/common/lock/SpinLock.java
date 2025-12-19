package com.samyookgoo.palgoosam.common.lock;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpinLock {

    private final RedisTemplate<String, Object> redisTemplate;

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
     * 락 획득 (재시도 포함)
     *
     * @param key              락 키
     * @param waitTimeMs       최대 대기 시간 (밀리초)
     * @param leaseTimeSeconds 락 자동 만료 시간 (초)
     * @return 락 식별자 (UUID), 실패 시 null
     */
    public String lock(String key, long waitTimeMs, long leaseTimeSeconds) {
        String lockValue = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        long retryInterval = 50; // 50ms마다 재시도

        while (System.currentTimeMillis() - startTime < waitTimeMs) {
            // 락 획득 시도
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key, lockValue, Duration.ofSeconds(leaseTimeSeconds));

            if (Boolean.TRUE.equals(acquired)) {
                log.info("락 획득 성공: key={}, value={}, elapsed={}ms",
                        key, lockValue, System.currentTimeMillis() - startTime);
                return lockValue;
            }

            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("락 획득 중 인터럽트: key={}", key);
                return null;
            }

            log.debug("락 획득 재시도: key={}, elapsed={}ms",
                    key, System.currentTimeMillis() - startTime);
        }

        log.warn("락 획득 실패 (타임아웃): key={}, waitTime={}ms", key, waitTimeMs);
        return null;
    }

    /**
     * 락 해제
     */
    public boolean unlock(String key, String lockValue) {
        if (lockValue == null) {
            return false;
        }

        Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(key),
                lockValue
        );

        return result != null && result == 1L;
    }
}
