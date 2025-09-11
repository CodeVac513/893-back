//package com.samyookgoo.palgoosam.config;
//
//import com.samyookgoo.palgoosam.auction.service.AuctionStatusService;
//import com.samyookgoo.palgoosam.common.event.LockReleaseSubscriber;
//import com.samyookgoo.palgoosam.schedule.AuctionStatusPublisher;
//import com.samyookgoo.palgoosam.schedule.AuctionStatusSubscriber;
//import com.samyookgoo.palgoosam.schedule.RedisKeyExpirationListener;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.data.redis.listener.PatternTopic;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//public class RedisListenerConfig {
//    private final RedisConnectionFactory connectionFactory;
//    private final AuctionStatusService auctionStatusService;
//    private final AuctionStatusPublisher statusPublisher;
//    private final AuctionStatusSubscriber statusSubscriber;
//    private final LockReleaseSubscriber lockReleaseSubscriber;
//    private final ChannelTopic auctionStatusTopic;
//    private final ChannelTopic lockReleaseTopic;
//
//    @Bean
//    public RedisMessageListenerContainer redisContainer() {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//
//        MessageListenerAdapter adapter = new MessageListenerAdapter(statusSubscriber, "onMessage");
//
//        adapter.setSerializer(new GenericJackson2JsonRedisSerializer());
//        container.addMessageListener(adapter, auctionStatusTopic);
//
//        container.addMessageListener(lockReleaseSubscriber, lockReleaseTopic);
//
//        return container;
//    }
//
//    @Bean
//    public RedisKeyExpirationListener rediskeyExpirationListener(RedisMessageListenerContainer container) {
//        RedisKeyExpirationListener listener =
//                new RedisKeyExpirationListener(container, auctionStatusService, statusPublisher);
//
//        container.addMessageListener(listener, new PatternTopic("__keyevent@*__:expired"));
//        return listener;
//    }
//}