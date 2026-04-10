package com.samyookgoo.palgoosam.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionWaiter {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition released = lock.newCondition();
    private Integer waitingCount = 0;

    public boolean await(Long timeoutMillis) {
        lock.lock();
        try {
            waitingCount++;
            return released.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            waitingCount--;
            lock.unlock();
        }
    }

    public void signalAll() {
        lock.lock();
        try {
            released.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean hasWaiters() {
        lock.lock();
        try {
            return waitingCount > 0;
        } finally {
            lock.unlock();
        }
    }
}
