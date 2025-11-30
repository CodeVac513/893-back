//package com.samyookgoo.palgoosam.bid.service.listener;
//
//import com.samyookgoo.palgoosam.bid.controller.response.BaseResponse;
//import com.samyookgoo.palgoosam.bid.controller.response.BidEventResponse;
//import com.samyookgoo.palgoosam.bid.controller.response.BidResponse;
//import com.samyookgoo.palgoosam.bid.controller.response.BidResultResponse;
//import com.samyookgoo.palgoosam.bid.service.SseService;
//import com.samyookgoo.palgoosam.bid.service.response.BidFailedEvent;
//import com.samyookgoo.palgoosam.bid.service.response.BidSuccessEvent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class BidEventListener {
//    private final SseService sseService;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleBidSuccess(BidSuccessEvent event) {
//        BidEventResponse eventData = BidEventResponse.builder()
//                .currentPrice(event.bidStats().getMaxPrice())
//                .totalBid(event.bidStats().getTotalBid())
//                .totalBidder(event.bidStats().getTotalBidder())
//                .isCancelled(false)
//                .bid(BidResponse.from(event.bid()))
//                .build();
//
//        BidResultResponse bidResult = BidResultResponse.from(
//                BidResponse.from(event.bid()),
//                event.canCancelBid()
//        );
//
//        sseService.sendBidResultToUser(event.userId(), BaseResponse.success(bidResult));
//        sseService.broadcastBidUpdate(event.auctionId(), eventData);
//
//        log.info("입찰 결과 전송!!!!!!!!!!!! auctionId = {}, price = {}", event.auctionId(), event.bid().getPrice());
//    }
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
//    public void handleBidFailure(BidFailedEvent event) {
//        sseService.sendBidResultToUser(event.userId(), BaseResponse.error(event.message(), null));
//
//        log.info("입찰 실패 전송............ userId = {}, message = {}", event.userId(), event.message());
//    }
//}
