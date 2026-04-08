package com.samyookgoo.palgoosam.common.service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String bidLockPrefix = "lock:auction:";

    public UUID lockWithRetry(Long auctionId) {
        UUID uuid = tryLock(auctionId);
        while (uuid == null) {
            uuid = tryLock(auctionId);
        }
        log.info("UUID[{}]: Lock 획득 성공", uuid.toString());
        return uuid;
    }

    private UUID tryLock(Long auctionId) {
        String key = bidLockPrefix + auctionId;
        UUID uuid = UUID.randomUUID();
        Duration timeout = Duration.ofSeconds(3);
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, uuid.toString(), timeout);

        return Boolean.TRUE.equals(result) ? uuid : null;
    }

    public Boolean unlock(Long auctionId, UUID uuid) {
        String key = bidLockPrefix + auctionId;
        String script = """
                    if redis.call("get", KEYS[1]) == ARGV[1] then
                        return redis.call("del", KEYS[1])
                    else
                        return 0
                    end
                """;
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), uuid.toString());

        return Long.valueOf(1L).equals(result);
    }
}
