//package com.samyookgoo.palgoosam.common.event;
//
//import com.samyookgoo.palgoosam.common.lock.LockRetryHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//
//@Component
//@RequiredArgsConstructor
//public class LockReleaseSubscriber implements MessageListener {
//
//    private final LockRetryHandler lockRetryHandler;
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        String lockKey = new String(message.getBody(), StandardCharsets.UTF_8);
//        lockRetryHandler.retry(lockKey);
//    }
//}