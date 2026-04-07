//package com.samyookgoo.palgoosam.common.service;
//
//import java.time.Duration;
//import java.util.Collections;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RedisLockService {
//    private final StringRedisTemplate redisTemplate;
//    private final static String CHANNEL = "lock.release";
//
//    public Boolean tryAcquire(String lockKey, Duration expireTime, String uniqueId) {
//        Boolean success = redisTemplate.opsForValue()
//                .setIfAbsent(lockKey, uniqueId, expireTime);
//
//        if (Boolean.TRUE.equals(success)) {
//            log.info("락 획득 성공: key={}, uniqueId={}, 만료={}", lockKey, uniqueId, expireTime);
//            return true;
//        } else {
//            log.info("락 획득 실패: key={}, uniqueId={}", lockKey, uniqueId);
//            return false;
//        }
//    }
//
//    public void release(String lockKey, String uniqueId) {
//        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
//                "return redis.call('del', KEYS[1]); else return 0; end";
//
//        Long result = redisTemplate.execute(
//                new DefaultRedisScript<>(script, Long.class),
//                Collections.singletonList(lockKey),
//                uniqueId
//        );
//
//        if (result != null && result > 0) {
//            log.info("락 해제 및 채널 발행: key={}, uniqueId={}", lockKey, uniqueId);
//            redisTemplate.convertAndSend(CHANNEL, lockKey);
//        } else {
//            log.warn("락 해제 실패 또는 락 소유자가 아님: key={}, uniqueId={}", lockKey, uniqueId);
//        }
//    }
//}