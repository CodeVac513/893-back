package com.samyookgoo.palgoosam.common.lock;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockListener {

    private final RedisMessageListenerContainer container;
    private final WaiterRegistry waiterRegistry;

    private static final String UNLOCK_CHANNEL = "unlock:auction";

    @PostConstruct
    public void init() {
        container.addMessageListener((message, pattern) -> {
            Long auctionId = Long.parseLong(message.toString());

            log.info("auctionId: {}번 auction의 Lock 해제 이벤트 publish", auctionId);
            AuctionWaiter waiter = waiterRegistry.get(auctionId);
            if(waiter!=null) {
                waiter.signalAll();
                waiterRegistry.removeIfNoWaiters(auctionId);
            }
        }, new ChannelTopic(UNLOCK_CHANNEL));
    }


}
