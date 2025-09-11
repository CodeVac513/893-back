//package com.samyookgoo.palgoosam.schedule;
//
//import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class AuctionStatusPublisher {
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final ChannelTopic auctionStatusTopic;
//
//    public void publishStatus(Long auctionId, AuctionStatus status) {
//        AuctionStatusPayload payload = new AuctionStatusPayload(auctionId, status);
//        redisTemplate.convertAndSend(auctionStatusTopic.getTopic(), payload);
//    }
//}
