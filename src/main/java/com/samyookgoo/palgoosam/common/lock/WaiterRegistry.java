package com.samyookgoo.palgoosam.common.lock;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class WaiterRegistry {
    private final ConcurrentHashMap<Long, AuctionWaiter> waiters = new ConcurrentHashMap<>();

    public AuctionWaiter getOrCreate(Long auctionId) {
        return waiters.computeIfAbsent(auctionId, id -> new AuctionWaiter());
    }

    public AuctionWaiter get(Long auctionId) {
        return waiters.get(auctionId);
    }

    public void removeIfNoWaiters(Long auctionId) {
        AuctionWaiter waiter = waiters.get(auctionId);
        if(waiter != null && waiter.hasWaiters()) {
            waiters.remove(auctionId, waiter);
        }
    }
}
