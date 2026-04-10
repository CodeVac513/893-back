package com.samyookgoo.palgoosam.common.service;

import com.samyookgoo.palgoosam.common.lock.AuctionWaiter;
import com.samyookgoo.palgoosam.common.lock.WaiterRegistry;
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
    private final WaiterRegistry waiterRegistry;

    private static final String BID_LOCK_PREFIX = "lock:auction:";
    private static final String UNLOCK_CHANNEL = "unlock:auction";
    private static final Duration LOCK_TTL = Duration.ofSeconds(3);

    public UUID lockWithRetry(Long auctionId) {
        UUID uuid = tryLock(auctionId);
        while (uuid == null) {
            uuid = tryLock(auctionId);
        }
        log.info("UUID[{}]: Lock 획득 성공", uuid.toString());
        return uuid;
    }

    public UUID lockWithWait(Long auctionId, Duration waitTimeout) {
        long deadline = System.currentTimeMillis() + waitTimeout.toMillis();

        while (System.currentTimeMillis() < deadline) {
            UUID uuid = tryLock(auctionId);
            if (uuid != null) {
                return uuid;
            }

            long remainingMillis = deadline - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                break;
            }

            AuctionWaiter waiter = waiterRegistry.getOrCreate(auctionId);

            uuid = tryLock(auctionId);
            if (uuid != null) {
                return uuid;
            }
            waiter.await(remainingMillis);
        }
        throw new IllegalStateException("락 획득 대기 시간 초과");
    }

    private UUID tryLock(Long auctionId) {
        String key = BID_LOCK_PREFIX + auctionId;
        UUID uuid = UUID.randomUUID();

        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, uuid.toString(), LOCK_TTL);

        return Boolean.TRUE.equals(result) ? uuid : null;
    }

    public Boolean unlock(Long auctionId, UUID uuid) {
        String key = BID_LOCK_PREFIX + auctionId;
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

    public void publishUnlock(Long auctionId) {
        redisTemplate.convertAndSend(UNLOCK_CHANNEL, auctionId);
    }
}
