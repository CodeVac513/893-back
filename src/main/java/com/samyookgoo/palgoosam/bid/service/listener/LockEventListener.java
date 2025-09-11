//package com.samyookgoo.palgoosam.bid.service.listener;
//
//import com.samyookgoo.palgoosam.bid.service.response.LockReleaseEvent;
//import com.samyookgoo.palgoosam.common.service.RedisLockService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//@Component
//@RequiredArgsConstructor
//public class LockEventListener {
//
//    private final RedisLockService redisLockService;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
//    public void handleLockRelease(LockReleaseEvent event) {
//        redisLockService.release(event.lockInfo().lockKey(), event.lockInfo().uniqueId());
//    }
//}