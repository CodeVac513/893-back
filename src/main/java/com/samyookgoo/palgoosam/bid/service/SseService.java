package com.samyookgoo.palgoosam.bid.service;

import com.samyookgoo.palgoosam.bid.controller.response.BidEventResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

//import com.samyookgoo.palgoosam.schedule.AuctionStatusEventResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {
    private final Map<Long, List<SseEmitter>> bidEmitters = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    public SseEmitter subscribe(Long auctionId) {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        bidEmitters.computeIfAbsent(auctionId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(auctionId, emitter));
        emitter.onTimeout(() -> removeEmitter(auctionId, emitter));
        emitter.onError((ex) -> removeEmitter(auctionId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public SseEmitter subscribePersonal(Long userId) {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        userEmitters.put(userId, emitter);

        emitter.onCompletion(() -> userEmitters.remove(userId));
        emitter.onTimeout(() -> userEmitters.remove(userId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void sendBidResultToUser(Long userId, Object result) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("bid-result")
                        .data(result));
            } catch (IOException e) {
                emitter.completeWithError(e);
                userEmitters.remove(userId);
            }
        }
    }

    public void broadcastBidUpdate(Long auctionId, BidEventResponse response) {
        List<SseEmitter> sseEmitters = bidEmitters.getOrDefault(auctionId, new ArrayList<>());
        List<SseEmitter> deadEmitters = new ArrayList<>();

        sseEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("bid-update")
                        .data(response));
            } catch (IOException e) {
                emitter.completeWithError(e);
                deadEmitters.add(emitter);
            }
        });

        sseEmitters.removeAll(deadEmitters);
    }

//    public void broadcastStatusUpdate(Long auctionId, AuctionStatusEventResponse response) {
//        List<SseEmitter> sseEmitters = bidEmitters.getOrDefault(auctionId, new ArrayList<>());
//        List<SseEmitter> deadEmitters = new ArrayList<>();
//
//        sseEmitters.forEach(emitter -> {
//            try {
//                emitter.send(SseEmitter.event()
//                        .name("status-update")
//                        .data(response));
//            } catch (IOException e) {
//                emitter.completeWithError(e);
//                deadEmitters.add(emitter);
//            }
//        });
//        sseEmitters.removeAll(deadEmitters);
//    }

    private void removeEmitter(Long auctionId, SseEmitter emitter) {
      List<SseEmitter> emitters = bidEmitters.get(auctionId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                bidEmitters.remove(auctionId);
            }
        }
    }

    @PostConstruct
    public void initHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            bidEmitters.forEach((auctionId, emitterList) -> {
                List<SseEmitter> deadEmitters = new ArrayList<>();
                for (SseEmitter emitter : emitterList) {
                    try {
                        emitter.send(SseEmitter.event().comment("heartbeat"));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                        deadEmitters.add(emitter);
                    }
                }
                emitterList.removeAll(deadEmitters); // 끊긴 emitter 제거
            });
        }, 0, 30, TimeUnit.SECONDS); // 30초 간격
    }

    @PreDestroy
    public void shutdownHeartbeatExecutor() {
        heartbeatExecutor.shutdown();
    }
}
