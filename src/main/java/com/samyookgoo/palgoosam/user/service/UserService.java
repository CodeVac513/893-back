package com.samyookgoo.palgoosam.user.service;

import com.samyookgoo.palgoosam.auction.domain.AuctionForMyPageProjection;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.bid.domain.BidForHighestPriceProjection;
import com.samyookgoo.palgoosam.bid.domain.BidForMyPageProjection;
import com.samyookgoo.palgoosam.bid.repository.BidRepository;
import com.samyookgoo.palgoosam.payment.domain.PaymentForMyPageProjection;
import com.samyookgoo.palgoosam.payment.repository.PaymentRepository;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.dto.UserAuctionsResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserBidsResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserInfoResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserPaymentsResponseDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(User currentUser) {
        return UserInfoResponseDto.from(currentUser);
    }

    @Transactional(readOnly = true)
    public List<UserBidsResponseDto> getUserBids(User currentUser) {
        List<BidForHighestPriceProjection> bids = bidRepository.findHighestBidProjectsByBidderId(currentUser.getId());
        Map<Long, Integer> maxBidMap = getMaxBidMap(bids);

        List<BidForMyPageProjection> bidProjections = bidRepository.findAllBidsByUserId(currentUser.getId());

        return bidProjections.stream()
                .map(bidProjection -> UserBidsResponseDto.of(bidProjection,
                        maxBidMap.getOrDefault(bidProjection.getAuctionId(), 0))).toList();
    }

    @Transactional(readOnly = true)
    public List<UserAuctionsResponseDto> getUserAuctions(User currentUser) {
        List<AuctionForMyPageProjection> auctions = auctionRepository.findAllAuctionProjectionBySellerId(
                currentUser.getId());

        List<BidForHighestPriceProjection> bids = bidRepository.findHighestBidProjectsBySellerId(currentUser.getId());
        Map<Long, Integer> maxBidMap = getMaxBidMap(bids);

        return createUserAuctionsResponseDtoList(auctions, maxBidMap);
    }

    @Transactional(readOnly = true)
    public List<UserAuctionsResponseDto> getUserScraps(User currentUser) {
        List<AuctionForMyPageProjection> auctions = auctionRepository.findAllAuctionProjectionWithScrapByUserId(
                currentUser.getId());

        List<BidForHighestPriceProjection> bids = bidRepository.findHighestBidProjectsByScraperId(currentUser.getId());
        Map<Long, Integer> maxBidMap = getMaxBidMap(bids);

        return createUserAuctionsResponseDtoList(auctions, maxBidMap);
    }

    @Transactional(readOnly = true)
    public List<UserPaymentsResponseDto> getUserPayments(User currentUser) {
        List<PaymentForMyPageProjection> payments = paymentRepository.findAllPaymentForMyPageProjectionByBuyerId(
                currentUser.getId());

        return payments.stream().map(UserPaymentsResponseDto::of).toList();
    }

    private Map<Long, Integer> getMaxBidMap(List<BidForHighestPriceProjection> bids) {
        return bids.stream().collect(Collectors.toMap(
                BidForHighestPriceProjection::getAuctionId,
                BidForHighestPriceProjection::getBidHighestPrice,
                (existing, replacement) -> existing
        ));
    }

    private List<UserAuctionsResponseDto> createUserAuctionsResponseDtoList(List<AuctionForMyPageProjection> auctions,
                                                                            Map<Long, Integer> maxBidMap) {
        return auctions.stream()
                .map(auctionProjection -> UserAuctionsResponseDto.of(
                        auctionProjection,
                        maxBidMap.getOrDefault(auctionProjection.getAuctionId(), 0)
                ))
                .toList();
    }
}
