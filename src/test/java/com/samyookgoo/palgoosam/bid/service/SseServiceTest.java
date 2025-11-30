package com.samyookgoo.palgoosam.bid.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.samyookgoo.palgoosam.bid.controller.response.BidEventResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

class SseServiceTest {

    private SseService sseService;

    @BeforeEach
    void setUp() {
        sseService = new SseService();
    }

    @DisplayName("subscribe 호출 시 emitter 가 등록되고 connect 이벤트가 전송된다.")
    @Test
    void registersEmitterAndSendsConnectEvent() {
        // given
        Long auctionId = 1L;

        // when
        SseEmitter emitter = sseService.subscribe(auctionId);

        // then
        assertThat(emitter).isNotNull();
    }

    @DisplayName("broadcastBidUpdate 호출 시 등록된 emitter 들에 이벤트가 전송된다.")
    @Test
    void sendsBidUpdateEventToEmitters() throws Exception {
        // given
        Long auctionId = 1L;

        SseEmitter emitterSpy = spy(new SseEmitter(Duration.ofMinutes(30).toMillis()));

        sseService.subscribe(auctionId);

        getEmittersList(auctionId).clear();
        getEmittersList(auctionId).add(emitterSpy);

        BidEventResponse response = BidEventResponse.builder()
                .build();

        // when
        sseService.broadcastBidUpdate(auctionId, response);

        // then
        ArgumentCaptor<SseEventBuilder> captor = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitterSpy).send(captor.capture());

        SseEmitter.SseEventBuilder eventSent = captor.getValue();
        assertThat(eventSent).isNotNull();
    }

    @DisplayName("IOException 발생 시 emitter 에 completeWithError() 가 호출된다.")
    @Test
    void completeWithErrorIsCalledWhenIOExceptionOccurs() throws Exception {
        // given
        Long auctionId = 1L;

        SseEmitter faultyEmitter = mock(SseEmitter.class);
        doThrow(new IOException("test exception")).when(faultyEmitter).send(any(SseEmitter.SseEventBuilder.class));
        getEmittersList(auctionId).add(faultyEmitter);

        BidEventResponse response = BidEventResponse.builder()
                .build();

        // when
        sseService.broadcastBidUpdate(auctionId, response);

        // then
        verify(faultyEmitter).completeWithError(any(IOException.class));
        assertThat(getEmittersList(auctionId)).doesNotContain(faultyEmitter);
    }

    @SuppressWarnings("unchecked")
    private List<SseEmitter> getEmittersList(Long auctionId) {
        try {
            var emittersField = SseService.class.getDeclaredField("emitters");
            emittersField.setAccessible(true);
            var emittersMap = (Map<Long, List<SseEmitter>>) emittersField.get(sseService);
            return emittersMap.computeIfAbsent(auctionId, id -> new CopyOnWriteArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}