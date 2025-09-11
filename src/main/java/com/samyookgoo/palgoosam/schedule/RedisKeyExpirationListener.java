//package com.samyookgoo.palgoosam.schedule;
//
//import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
//import com.samyookgoo.palgoosam.auction.service.AuctionStatusService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//
//@Slf4j
//public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
//    private final AuctionStatusService auctionStatusService;
//    private final AuctionStatusPublisher auctionPublisher;
//
//    public RedisKeyExpirationListener(
//            RedisMessageListenerContainer listenerContainer,
//            AuctionStatusService auctionStatusService,
//            AuctionStatusPublisher auctionPublisher
//    ) {
//        super(listenerContainer);
//        this.auctionStatusService = auctionStatusService;
//        this.auctionPublisher = auctionPublisher;
//    }
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        String expiredKey = message.toString();
//        log.info("@@@@@ Redis 키 만료 이벤트 수신: {}", expiredKey);
//
//        if (expiredKey.startsWith("auction:trigger:start:")) {
//            Long id = Long.parseLong(expiredKey.split(":")[3]);
//            auctionStatusService.updateStatusToActive(id);
//            auctionPublisher.publishStatus(id, AuctionStatus.active);
//        }
//        else if (expiredKey.startsWith("auction:trigger:end:")) {
//            Long id = Long.parseLong(expiredKey.split(":")[3]);
//            auctionStatusService.updateStatusToCompleted(id);
//            auctionPublisher.publishStatus(id, AuctionStatus.completed);
//        }
//    }
//}
