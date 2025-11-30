package com.samyookgoo.palgoosam.bid.service;

import com.samyookgoo.palgoosam.bid.controller.response.BidEventResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long auctionId) {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        emitters.computeIfAbsent(auctionId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(auctionId, emitter));
        emitter.onTimeout(() -> removeEmitter(auctionId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void broadcastBidUpdate(Long auctionId, BidEventResponse response) {
        List<SseEmitter> sseEmitters = emitters.getOrDefault(auctionId, new ArrayList<>());
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

    private void removeEmitter(Long auctionId, SseEmitter emitter) {
        emitters.getOrDefault(auctionId, List.of()).remove(emitter);
    }
}
