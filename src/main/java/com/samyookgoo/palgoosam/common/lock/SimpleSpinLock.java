package com.samyookgoo.palgoosam.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleSpinLock {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 락 획득 시도
     *
     * @param key     락 키
     * @param value   락 소유자 식별값
     * @param timeout 만료 시간 (second)
     * @return 획득 성공 여부
     */
    public boolean tryLock(String key, String value, long timeout) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofSeconds(timeout));

        log.info("락 획득 시도: key={}, value={}, result={}",  key, value, result);

        return Boolean.TRUE.equals(result);
    }

    /**
     * 락 해제
     */
    public void unlock(String key) {
        redisTemplate.delete(key);
        log.info("락 해제: key={}", key);
    }
}
