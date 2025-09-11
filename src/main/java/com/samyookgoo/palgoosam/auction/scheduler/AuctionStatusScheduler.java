//package com.samyookgoo.palgoosam.auction.scheduler;
//
//import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
//import com.samyookgoo.palgoosam.auction.domain.Auction;
//import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import com.samyookgoo.palgoosam.auction.service.AuctionStatusService;
////import com.samyookgoo.palgoosam.schedule.AuctionStatusPublisher;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class AuctionStatusScheduler {
//    private final AuctionRepository auctionRepository;
//    private final AuctionStatusService auctionStatusService;
////    private final AuctionStatusPublisher statusPublisher;
//
//    @Scheduled(fixedRate = 300000)  // 5분마다 실행
//    @Transactional
//    public void FallbackAuctionStatus() {
//        LocalDateTime now = LocalDateTime.now();
//
//        List<Auction> shouldBeActive = auctionRepository.findByStatusAndStartTimeBefore(AuctionStatus.pending, now);
//        for(Auction auction : shouldBeActive) {
//            auctionStatusService.updateStatusToActive(auction);
//            statusPublisher.publishStatus(auction.getId(), AuctionStatus.active);
//        }
//
//        List<Auction> shouldBeCompleted = auctionRepository.findByStatusAndEndTimeBefore(AuctionStatus.active, now);
//        for(Auction auction : shouldBeCompleted) {
//            auctionStatusService.updateStatusToCompleted(auction);
//            statusPublisher.publishStatus(auction.getId(), AuctionStatus.completed);
//        }
//    }
//}
