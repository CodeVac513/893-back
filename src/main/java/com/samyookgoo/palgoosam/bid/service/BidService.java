package com.samyookgoo.palgoosam.bid.service;

import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.exception.AuctionNotFoundException;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.bid.controller.response.BidEventResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidOverviewResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidResultResponse;
import com.samyookgoo.palgoosam.bid.domain.Bid;
import com.samyookgoo.palgoosam.bid.exception.BidBadRequestException;
import com.samyookgoo.palgoosam.bid.exception.BidConflictException;
import com.samyookgoo.palgoosam.bid.exception.BidInvalidStateException;
import com.samyookgoo.palgoosam.bid.exception.BidNotFoundException;
import com.samyookgoo.palgoosam.bid.repository.BidRepository;
import com.samyookgoo.palgoosam.bid.service.response.BidStatsResponse;
import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final SseService sseService;

    @Transactional(readOnly = true)
    public BidOverviewResponse getBidOverview(Long auctionId, User user) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new AuctionNotFoundException();
        }

        List<Bid> allBids = getByAuctionIdOrderByCreatedAtDesc(auctionId);

        Map<Boolean, List<Bid>> partitioned = allBids.stream()
                .collect(Collectors.partitioningBy(Bid::isCancelled));

        List<BidResponse> activeBids = partitioned.get(false).stream()
                .map(BidResponse::from)
                .collect(Collectors.toList());

        List<BidResponse> cancelledBids = partitioned.get(true).stream()
                .map(BidResponse::from)
                .collect(Collectors.toList());

        BidResponse recentUserBid = null;
        if (user != null && !hasUserCancelledBid(auctionId, user.getId())) {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            recentUserBid = findRecentUserBid(partitioned.get(false), user.getId(), oneMinuteAgo);
        }

        BidStatsResponse bidStats = bidRepository.findBidStatsByAuctionId(auctionId);
        return BidOverviewResponse.builder()
                .auctionId(auctionId)
                .currentPrice(bidStats.getMaxPrice())
                .totalBid(bidStats.getTotalBid())
                .totalBidder(bidStats.getTotalBidder())
                .canCancelBid(recentUserBid != null)
                .recentUserBid(recentUserBid)
                .bids(activeBids)
                .cancelledBids(cancelledBids)
                .build();
    }

    @Transactional
    public BidResultResponse placeBid(Long auctionId, User user, int price) {
        try {
            log.info("=== [START] placeBid - auctionId: {}, userId: {}, price: {}",
                    auctionId, user.getId(), price);

            // 1. Auction 조회
            log.info(">>> [STEP 1] Auction 조회 시작");
            Auction auction = auctionRepository.findById(auctionId)
                    .orElseThrow(AuctionNotFoundException::new);
            log.info(">>> [STEP 1] Auction 조회 완료 - version: {}", auction.getVersion());

            LocalDateTime now = LocalDateTime.now();
            // 2. 검증 및 Bid 생성
            log.info(">>> [STEP 2] Bid 생성 시작");
            Bid newBid = createValidatedBid(auction, user, price, now);
            log.info(">>> [STEP 2] Bid 생성 완료");

            log.info(">>> [STEP 3] 이전 Bid 비활성화");
            deactivatePreviousWinningBid(auctionId);
            log.info(">>> [STEP 4] 새 Bid 저장");
            bidRepository.save(newBid);
            log.info(">>> [STEP 4] 새 Bid 저장 완료 (아직 flush 안됨)");


            // 3. Auction 업데이트
            log.info(">>> [STEP 5] Auction 업데이트 시작 - 현재 version: {}",
                    auction.getVersion());
            auction.setUpdatedAt(now);
            log.info(">>> [STEP 5] Auction 수정 완료 (메모리상, 아직 flush 안됨)");

            log.info(">>> [STEP 6] Auction save 호출");
            auctionRepository.save(auction);
            log.info(">>> [STEP 6] Auction save 완료 (아직 flush 안됨)");

            log.info(">>> [STEP 7] SSE 이벤트 발송");
            BidEventResponse event = createBidEventResponse(auctionId, newBid, false);
            sseService.broadcastBidUpdate(auctionId, event);

            boolean canCancelBid = !hasUserCancelledBid(auctionId, user.getId());

            log.info("=== [END] placeBid 완료 (트랜잭션 커밋 전)");
            return BidResultResponse.from(BidResponse.from(newBid), canCancelBid);

        } catch (OptimisticLockingFailureException e) {
            log.warn(">>> [ERROR] Optimistic Lock 충돌 발생!");
            throw new BidConflictException(ErrorCode.BID_OPTIMISTIC_LOCK_FAILED);
        } catch (Exception e) {
            log.error(">>> [ERROR] 예상치 못한 에러: {}", e.getMessage(), e);
        }
        return null;
    }

    @Transactional
    public void cancelBid(Long auctionId, Long bidId, Long userId, LocalDateTime now) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new AuctionNotFoundException();
        }

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(BidNotFoundException::new);

        validateBidCancelable(auctionId, userId, bid, now);

        bid.cancel();

        activateNewWinningBid(auctionId);

        BidEventResponse event = createBidEventResponse(auctionId, bid, true);
        sseService.broadcastBidUpdate(auctionId, event);
    }

    private List<Bid> getByAuctionIdOrderByCreatedAtDesc(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId);
    }

    private BidResponse findRecentUserBid(List<Bid> activeBids, Long userId, LocalDateTime thresholdTime) {
        return activeBids.stream()
                .filter(bid -> bid.isOwner(userId) && bid.getCreatedAt().isAfter(thresholdTime))
                .findFirst()
                .map(BidResponse::from)
                .orElse(null);
    }

    private void validateBidCancelable(Long auctionId, Long userId, Bid bid, LocalDateTime now) {
        bid.validateCancelConditions(userId, now);

        if (hasUserCancelledBid(auctionId, userId)) {
            throw new BidInvalidStateException(ErrorCode.BID_CANCEL_LIMIT_EXCEEDED);
        }
    }

    private boolean hasUserCancelledBid(Long auctionId, Long userId) {
        return bidRepository.existsByAuctionIdAndBidderIdAndIsDeletedTrue(auctionId, userId);
    }

    private Bid createValidatedBid(Auction auction, User user, int price, LocalDateTime now) {
        if (price > 1_000_000_000) {
            throw new BidBadRequestException(ErrorCode.BID_EXCEEDS_MAXIMUM);
        }

        auction.validateBidConditions(user.getId(), price, now);
        validatePriceIsHighest(auction.getId(), price);

        return Bid.placeBy(auction, user, price);
    }

    private void deactivatePreviousWinningBid(Long auctionId) {
        bidRepository.findTopValidBidByAuctionId(auctionId)
                .ifPresent(prev -> prev.setIsWinning(false));
    }

    private void activateNewWinningBid(Long auctionId) {
        bidRepository.findTopValidBidByAuctionId(auctionId)
                .ifPresent(newWinner -> newWinner.setIsWinning(true));
    }


    private void validatePriceIsHighest(Long auctionId, int price) {
        Integer highestPrice = bidRepository.findMaxBidPriceByAuctionId(auctionId);
        if (highestPrice != null && price <= highestPrice) {
            throw new BidBadRequestException(ErrorCode.BID_NOT_HIGHEST);
        }
    }

    private BidEventResponse createBidEventResponse(Long auctionId, Bid bid, boolean isCancelled) {
        BidStatsResponse bidStats = bidRepository.findBidStatsByAuctionId(auctionId);
        return BidEventResponse.builder()
                .currentPrice(bidStats.getMaxPrice())
                .totalBid(bidStats.getTotalBid())
                .totalBidder(bidStats.getTotalBidder())
                .isCancelled(isCancelled)
                .bid(BidResponse.from(bid))
                .build();
    }
}
