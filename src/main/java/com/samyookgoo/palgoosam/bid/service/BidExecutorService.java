//package com.samyookgoo.palgoosam.bid.service;
//
//import com.samyookgoo.palgoosam.auction.domain.Auction;
//import com.samyookgoo.palgoosam.auction.exception.AuctionNotFoundException;
//import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
//import com.samyookgoo.palgoosam.bid.domain.Bid;
//import com.samyookgoo.palgoosam.bid.exception.BidBadRequestException;
//import com.samyookgoo.palgoosam.bid.repository.BidRepository;
//import com.samyookgoo.palgoosam.bid.service.response.BidFailedEvent;
//import com.samyookgoo.palgoosam.bid.service.response.BidStatsResponse;
//import com.samyookgoo.palgoosam.bid.service.response.BidSuccessEvent;
//import com.samyookgoo.palgoosam.bid.service.response.LockReleaseEvent;
//import com.samyookgoo.palgoosam.common.lock.LockInfo;
//import com.samyookgoo.palgoosam.global.exception.ErrorCode;
//import com.samyookgoo.palgoosam.user.domain.User;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class BidExecutorService {
//
//    private final BidRepository bidRepository;
//    private final AuctionRepository auctionRepository;
//    private final ApplicationEventPublisher eventPublisher;
//
//    @Transactional
//    public void placeBid(Long auctionId, User user, int price, LockInfo lockInfo) {
//        try {
//            Auction auction = auctionRepository.findById(auctionId)
//                    .orElseThrow(AuctionNotFoundException::new);
//
//            LocalDateTime now = LocalDateTime.now();
//            Bid newBid = placeBidWithValidation(auction, user, price, now);
//
//            BidStatsResponse bidStats = getBidStatsByAuctionId(auctionId);
//            boolean canCancel = !hasUserCancelledBid(auctionId, user.getId());
//
//            eventPublisher.publishEvent(new BidSuccessEvent(user.getId(), auctionId, newBid, bidStats, canCancel));
//        } catch (RuntimeException e) {
//            eventPublisher.publishEvent(new BidFailedEvent(user.getId(), e.getMessage()));
//            throw e;
//        } finally {
//            eventPublisher.publishEvent(new LockReleaseEvent(lockInfo));
//        }
//    }
//
//    private Bid placeBidWithValidation(Auction auction, User user, int price, LocalDateTime now) {
//        if (price > 1_000_000_000) {
//            throw new BidBadRequestException(ErrorCode.BID_EXCEEDS_MAXIMUM);
//        }
//
//        auction.validateBidConditions(user.getId(), price, now);
//
//        Optional<Bid> winningBid = bidRepository.findTopBidByAuctionIdOrderByPriceDesc(auction.getId());
//
//        winningBid.ifPresent(bid -> {
//            bid.validatePriceIsHigherThan(price);
//            bid.lose();
//            log.info("이전 최고 입찰 업데이트. 이전 최고 입찰가  : {}", bid.getPrice());
//        });
//
//        Bid newBid = Bid.placeBy(auction, user, price);
//
//        try {
//            log.info("입찰 등록 성공. 현재 최고 입찰가: {}", price);
//            return bidRepository.save(newBid);
//        } catch (DataIntegrityViolationException e) {
//            log.info("입찰 등록 실패. 입찰가: {}", price);
//            throw new BidBadRequestException(ErrorCode.BID_NOT_HIGHEST);
//        }
//    }
//
//    private BidStatsResponse getBidStatsByAuctionId(Long auctionId) {
//        return bidRepository.findBidStatsByAuctionId(auctionId);
//    }
//
//    private boolean hasUserCancelledBid(Long auctionId, Long userId) {
//        return bidRepository.existsByAuctionIdAndBidderIdAndIsDeletedTrue(auctionId, userId);
//    }
//}
