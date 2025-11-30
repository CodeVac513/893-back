package com.samyookgoo.palgoosam.bid.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.exception.AuctionNotFoundException;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.auction.repository.CategoryRepository;
import com.samyookgoo.palgoosam.bid.controller.response.BidOverviewResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidResultResponse;
import com.samyookgoo.palgoosam.bid.domain.Bid;
import com.samyookgoo.palgoosam.bid.exception.BidBadRequestException;
import com.samyookgoo.palgoosam.bid.exception.BidForbiddenException;
import com.samyookgoo.palgoosam.bid.exception.BidInvalidStateException;
import com.samyookgoo.palgoosam.bid.repository.BidRepository;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class BidServiceTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private BidService bidService;

    @AfterEach
    void tearDown() {
        bidRepository.deleteAllInBatch();
        auctionRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("(판매자가 아닌) 일반 회원이 입찰하면 성공한다.")
    @Test
    void regularUserCanPlaceBid() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user, 1500);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), 1500);

    }

    @DisplayName("판매자가 자신의 경매에 입찰하면 예외가 발생한다.")
    @Test
    void sellerCannotPlaceBidOnOwnAuction() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(auction.getId(), auction.getSeller(), 1500))
                .isInstanceOf(BidForbiddenException.class)
                .hasMessage("판매자는 자신의 경매에 입찰할 수 없습니다.");
    }

    @DisplayName("입찰가가 최고가보다 클 경우 입찰에 성공 한다.")
    @Test
    void shouldSucceedWhenBidPriceIsHighest() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.save(user1);
        userRepository.save(user2);

        Bid bid = createBid(auction, user1, 1500, true, false);
        bidRepository.save(bid);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user2, 2000);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user2.getName()), 2000);
    }

    @DisplayName("입찰가가 최고가 일 경우 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenBidPriceIsHighest() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.save(user1);
        userRepository.save(user2);

        Bid bid = createBid(auction, user1, 1500, true, false);
        bidRepository.save(bid);

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(auction.getId(), user2, 1500))
                .isInstanceOf(BidBadRequestException.class)
                .hasMessage("현재 최고가보다 높은 금액을 입력해야 합니다.");
    }

    @DisplayName("입찰가가 최고가보다 작을 경우 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenBidPriceIsNotHighest() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.save(user1);
        userRepository.save(user2);

        Bid bid = createBid(auction, user1, 1500, true, false);
        bidRepository.save(bid);

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(auction.getId(), user2, 1400))
                .isInstanceOf(BidBadRequestException.class)
                .hasMessage("현재 최고가보다 높은 금액을 입력해야 합니다.");
    }

    @DisplayName("입찰가가 시작가보다 클 경우 입찰에 성공 한다.")
    @Test
    void shouldSucceedWhenBidPriceIsHigherThanStartPrice() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user, 2000);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), 2000);
    }

    @DisplayName("입찰가가 시작가 일 경우 입찰에 성공한다.")
    @Test
    void shouldThrowWhenBidPriceEqualsStartPrice() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user,
                auction.getBasePrice());

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), auction.getBasePrice());
    }

    @DisplayName("입찰가가 시작가보다 작을 경우 예외가 발생한다.")
    @Test
    void shouldThrowWhenBidPriceIsLowerThanStartPrice() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(auction.getId(), user, 900))
                .isInstanceOf(BidBadRequestException.class)
                .hasMessage("시작가보다 높은 금액을 입력해야 합니다.");
    }

    @DisplayName("입찰가가 10억보다 작을 경우 입찰에 성공한다.")
    @Test
    void shouldThrowWhenBidPriceIsLowerThanTenBillion() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(999_000_000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user, 999_999_000);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), 999_999_000);
    }

    @DisplayName("입찰가가 10억 일 경우 입찰에 성공한다.")
    @Test
    void shouldSucceedWhenBidPriceEqualsTenBillion() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(999_000_000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user, 1_000_000_000);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), 1_000_000_000);
    }

    @DisplayName("입찰가가 10억보다 클 경우 예외가 발생한다.")
    @Test
    void shouldSucceedWhenBidPriceIsGreaterThanTenBillion() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(999_000_000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(auction.getId(), user, 1_000_000_100))
                .isInstanceOf(BidBadRequestException.class)
                .hasMessage("입찰 금액은 최대 10억까지 가능합니다.");
    }

    @DisplayName("경매 중 입찰 시 입찰이 성공한다.")
    @Test
    void shouldSucceedWhenBiddingDuringAuction() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user, 1500);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), 1500);
    }

    @DisplayName("경매 종료 후 입찰 시 예외가 발생한다.")
    @Test
    void shouldFailBidAfterAuctionEnds() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now.minusHours(2), now.minusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(auction.getId(), user, 1500))
                .isInstanceOf(BidInvalidStateException.class)
                .hasMessage("현재는 입찰 가능한 시간이 아닙니다.");
    }

    @DisplayName("경매 시작 전 입찰 시 예외가 발생한다.")
    @Test
    void shouldFailBidBeforeAuctionStarts() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now.plusHours(1), now.plusHours(2));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(auction.getId(), user, 1500))
                .isInstanceOf(BidInvalidStateException.class)
                .hasMessage("현재는 입찰 가능한 시간이 아닙니다.");
    }

    @DisplayName("입찰 성공 시, 회원이 해당 경매에 취소한 이력이 없으면 취소 가능 여부는 true로 반환한다.")
    @Test
    void shouldAllowCancellationWhenNoPreviousCancellationExists() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid = createBid(auction, user, 1300, true, false);
        bidRepository.save(bid);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user, 1500);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getCanCancelBid()).isTrue();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), 1500);

    }

    @DisplayName("입찰 성공 시, 회원이 해당 경매에 취소한 이력이 있으면 취소 가능 여부를 false로 반환한다.")
    @Test
    void shouldNotAllowCancellationWhenPreviousCancellationExists() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid = createBid(auction, user, 1300, false, true);
        bidRepository.save(bid);

        //when
        BidResultResponse bidResultResponse = bidService.placeBid(auction.getId(), user, 1500);

        //then
        assertThat(bidResultResponse).isNotNull();
        assertThat(bidResultResponse.getCanCancelBid()).isFalse();
        assertThat(bidResultResponse.getBid()).isNotNull();
        assertThat(bidResultResponse.getBid())
                .extracting("bidderEmail", "bidPrice")
                .contains(maskName(user.getName()), 1500);
    }

    @DisplayName("존재 하지 않는 경매 상품에 대한 입찰 시, 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenBiddingOnNonExistingAuction() {
        //given
        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Long invalidAuctionId = -1L;

        //when & then
        assertThatThrownBy(() -> bidService.placeBid(invalidAuctionId, user, 1500))
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessage("해당 경매 상품이 존재하지 않습니다.");
    }

    @DisplayName("입찰자가 본인의 입찰을 취소하면 성공한다.")
    @Test
    void shouldCancelBidSuccessfully() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid = createBid(auction, user, 1500, true, false);
        bidRepository.save(bid);

        //when
        bidService.cancelBid(auction.getId(), bid.getId(), user.getId(), LocalDateTime.now());

        //then
        Bid cancelledBid = bidRepository.findById(bid.getId()).orElseThrow();
        assertThat(cancelledBid.isCancelled()).isTrue();
        assertThat(cancelledBid.getIsWinning()).isFalse();
    }

    @DisplayName("입찰자가 아닌 사용자가 입찰 취소할 경우 예외가 발생한다.")
    @Test
    void shouldThrowWhenUserIsNotBidder() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.save(user1);
        userRepository.save(user2);

        Bid bid = createBid(auction, user1, 1500, true, false);
        bidRepository.save(bid);

        //when & then
        assertThatThrownBy(() -> bidService.cancelBid(auction.getId(), bid.getId(), user2.getId(), LocalDateTime.now()))
                .isInstanceOf(BidForbiddenException.class)
                .hasMessage("본인의 입찰만 취소할 수 있습니다.");
    }

    @DisplayName("입찰자는 해당 경매에 대한 취소 이력이 없으면, 입찰 취소가 가능 하다.")
    @Test
    void shouldAllowOnlyOneCancelPerAuction() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.saveAll(List.of(user1, user2));

        Bid bid1 = createBid(auction, user1, 1200, false, false);
        Bid bid2 = createBid(auction, user2, 1300, false, false);
        Bid bid3 = createBid(auction, user1, 1400, true, false);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        // when
        bidService.cancelBid(auction.getId(), bid3.getId(), user1.getId(), LocalDateTime.now());

        // then
        Bid cancelledBid = bidRepository.findById(bid3.getId()).orElseThrow();
        assertThat(cancelledBid.isCancelled()).isTrue();
        assertThat(cancelledBid.getIsWinning()).isFalse();
    }

    @DisplayName("입찰자가 경매당 두 번 입찰 취소를 시도하면 예외가 발생한다.")
    @Test
    void shouldThrowWhenBidCancelledTwiceInSameAuction() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, true, false);
        bidRepository.save(bid1);

        bidService.cancelBid(auction.getId(), bid1.getId(), user.getId(), LocalDateTime.now());

        Bid bid2 = createBid(auction, user, 1500, true, false);
        bidRepository.save(bid2);

        // when & then
        assertThatThrownBy(() -> bidService.cancelBid(auction.getId(), bid2.getId(), user.getId(), LocalDateTime.now()))
                .isInstanceOf(BidInvalidStateException.class)
                .hasMessage("입찰 취소는 1회까지만 가능합니다.");
    }

    @DisplayName("입찰 후 1분 이내에는 입찰 취소가 가능하다.")
    @Test
    void shouldCancelBidWithinOneMinute() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid = createBid(auction, user, 1500, true, false);
        bidRepository.save(bid);

        // when
        bidService.cancelBid(auction.getId(), bid.getId(), user.getId(), bid.getCreatedAt().plusSeconds(30));

        // then
        Bid cancelledBid = bidRepository.findById(bid.getId()).orElseThrow();
        assertThat(cancelledBid.isCancelled()).isTrue();
    }

    @DisplayName("입찰 후 1분이 지나면 입찰 취소가 불가능하다.")
    @Test
    void shouldNotCancelBidAfterOneMinute() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid = createBid(auction, user, 1500, true, false);
        bid.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        bidRepository.save(bid);

        // when & then
        assertThatThrownBy(() -> bidService.cancelBid(auction.getId(), bid.getId(), user.getId(),
                bid.getCreatedAt().plusMinutes(2)))
                .isInstanceOf(BidInvalidStateException.class)
                .hasMessage("입찰 후 1분 이내에만 취소할 수 있습니다.");
    }

    @DisplayName("입찰 후 정확히 1분이 지난 경우 취소가 가능하다.")
    @Test
    void shouldCancelBidExactlyAtOneMinute() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid = createBid(auction, user, 1500, true, false);
        bidRepository.save(bid);

        // when
        bidService.cancelBid(auction.getId(), bid.getId(), user.getId(), bid.getCreatedAt().plusMinutes(1));

        // then
        Bid cancelledBid = bidRepository.findById(bid.getId()).orElseThrow();
        assertThat(cancelledBid.isCancelled()).isTrue();
    }

    @DisplayName("입찰 취소 시, 취소된 입찰 다음으로 큰 입찰가를 제시한 입찰이 최고 입찰이 된다.")
    @Test
    void shouldPromoteNextHighestBidWhenCancellingTopBid() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.saveAll(List.of(user1, user2));

        Bid bid1 = createBid(auction, user1, 1200, false, false);
        Bid bid2 = createBid(auction, user2, 1300, false, false);
        Bid bid3 = createBid(auction, user1, 1400, true, false);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        // when
        bidService.cancelBid(auction.getId(), bid3.getId(), user1.getId(), LocalDateTime.now());

        // then
        Bid cancelledBid = bidRepository.findById(bid3.getId()).orElseThrow();
        assertThat(cancelledBid.isCancelled()).isTrue();
        assertThat(cancelledBid.getIsWinning()).isFalse();

        Bid winningBid = bidRepository.findTopValidBidByAuctionId(auction.getId()).orElseThrow();
        assertThat(winningBid.getIsWinning()).isTrue();
        assertThat(winningBid.getPrice()).isEqualTo(1300);
    }

    @DisplayName("존재 하지 않는 경매 상품에 대한 입찰 취소 요청 시, 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenCancellingBidOnNonExistingAuction() {
        // given
        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Long invalidAuctionId = -1L;
        Long invalidBIdId = -1L;

        // when & then
        assertThatThrownBy(
                () -> bidService.cancelBid(invalidAuctionId, invalidBIdId, user.getId(), LocalDateTime.now()))
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessage("해당 경매 상품이 존재하지 않습니다.");
    }

    @DisplayName("비회원일 경우 경매 상세 페이지 진입 시, 입찰 관련 정보들을 제공한다.")
    @Test
    void shouldProvideBidInfoForGuest() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1200, false, false);
        Bid bid2 = createBid(auction, user, 1300, false, true);
        Bid bid3 = createBid(auction, user, 1400, false, true);
        Bid bid4 = createBid(auction, user, 1500, true, false);
        bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4));

        //when
        BidOverviewResponse response = bidService.getBidOverview(auction.getId(), null);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getCanCancelBid()).isFalse();
        assertThat(response.getRecentUserBid()).isNull();

        assertThat(response.getCurrentPrice()).isEqualTo(1500);
        assertThat(response.getTotalBid()).isEqualTo(2);
        assertThat(response.getTotalBidder()).isEqualTo(1);

        List<BidResponse> activeBids = response.getBids();
        assertThat(activeBids).hasSize(2);
        assertThat(activeBids.get(0).getBidPrice()).isEqualTo(1500);
        assertThat(activeBids.get(1).getBidPrice()).isEqualTo(1200);

        List<BidResponse> cancelledBids = response.getCancelledBids();
        assertThat(cancelledBids).hasSize(2);
        assertThat(cancelledBids.get(0).getBidPrice()).isEqualTo(1400);
        assertThat(cancelledBids.get(1).getBidPrice()).isEqualTo(1300);
    }

    @DisplayName("회원이 경매 상세 페이지 진입시, '최근 1분 내 입찰 했고 취소한 적이 없을 경우' 경매의 입찰 관련 정보와 유저의 취소 가능한 입찰 정보를 함께 제공한다. ")
    @Test
    void shouldProvideCancelableBidForMemberWithRecentBid() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.saveAll(List.of(user1, user2));

        Bid bid1 = createBid(auction, user1, 1200, false, false);
        Bid bid2 = createBid(auction, user1, 1300, false, false);
        Bid bid3 = createBid(auction, user1, 1400, false, false);
        Bid bid4 = createBid(auction, user2, 1500, true, false);
        bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4));

        //when
        BidOverviewResponse response = bidService.getBidOverview(auction.getId(), user2);

        //then
        assertThat(response.getCanCancelBid()).isTrue();
        assertThat(response.getRecentUserBid()).isNotNull();
        assertThat(response.getRecentUserBid().getBidPrice()).isEqualTo(1500);

        assertThat(response.getCurrentPrice()).isEqualTo(1500);
        assertThat(response.getTotalBid()).isEqualTo(4);
        assertThat(response.getTotalBidder()).isEqualTo(2);

        List<BidResponse> activeBids = response.getBids();
        assertThat(activeBids).hasSize(4);
        assertThat(activeBids.get(0).getBidPrice()).isEqualTo(1500);
        assertThat(activeBids.get(1).getBidPrice()).isEqualTo(1400);
        assertThat(activeBids.get(2).getBidPrice()).isEqualTo(1300);
        assertThat(activeBids.get(3).getBidPrice()).isEqualTo(1200);

        List<BidResponse> cancelledBids = response.getCancelledBids();
        assertThat(cancelledBids).isEmpty();

    }

    @DisplayName("회원이 경매 상세 페이지 진입시, '최근 1분 내 입찰 한적이 있지만 취소한 적 있는 경우' 취소 가능 관련 필드는 false, null로 반환된다.")
    @Test
    void shouldReturnNullForMemberIfRecentBidIsCancelled() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.saveAll(List.of(user1, user2));

        Bid bid1 = createBid(auction, user1, 1200, false, false);
        Bid bid2 = createBid(auction, user2, 1300, false, true);
        Bid bid3 = createBid(auction, user1, 1400, false, false);
        Bid bid4 = createBid(auction, user2, 1500, true, false);
        bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4));

        //when
        BidOverviewResponse response = bidService.getBidOverview(auction.getId(), user2);

        //then
        assertThat(response.getCanCancelBid()).isFalse();
        assertThat(response.getRecentUserBid()).isNull();

        assertThat(response.getCurrentPrice()).isEqualTo(1500);
        assertThat(response.getTotalBid()).isEqualTo(3);
        assertThat(response.getTotalBidder()).isEqualTo(2);

        List<BidResponse> activeBids = response.getBids();
        assertThat(activeBids).hasSize(3);
        assertThat(activeBids.get(0).getBidPrice()).isEqualTo(1500);
        assertThat(activeBids.get(1).getBidPrice()).isEqualTo(1400);
        assertThat(activeBids.get(2).getBidPrice()).isEqualTo(1200);

        List<BidResponse> cancelledBids = response.getCancelledBids();
        assertThat(cancelledBids).hasSize(1);
        assertThat(cancelledBids.getFirst().getBidPrice()).isEqualTo(1300);
    }

    @DisplayName("회원이 경매 상세 페이지 진입시, '입찰이 존재하지만 1분이 경과한 경우' 취소 가능 관련 필드는 false, null로 반환된다.")
    @Test
    void shouldReturnNullForMemberIfRecentBidIsTooOld() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now.minusHours(1), now.plusHours(1));
        auctionRepository.save(auction);

        User user1 = createUser("user1@test.com", "유저1");
        User user2 = createUser("user2@test.com", "유저2");
        userRepository.saveAll(List.of(user1, user2));

        Bid bid1 = createBid(auction, user1, 1400, false, false);
        Bid bid2 = createBid(auction, user2, 1500, true, false);
        Bid savedBid1 = bidRepository.save(bid1);
        Bid savedBid2 = bidRepository.save(bid2);

        savedBid1.setCreatedAt(now.minusMinutes(3));
        savedBid2.setCreatedAt(now.minusMinutes(2));
        bidRepository.saveAll(List.of(savedBid1, savedBid2));

        //when
        BidOverviewResponse response = bidService.getBidOverview(auction.getId(), user2);

        //then
        assertThat(response.getCanCancelBid()).isFalse();
        assertThat(response.getRecentUserBid()).isNull();

        assertThat(response.getCurrentPrice()).isEqualTo(1500);
        assertThat(response.getTotalBid()).isEqualTo(2);
        assertThat(response.getTotalBidder()).isEqualTo(2);

        List<BidResponse> activeBids = response.getBids();
        assertThat(activeBids).hasSize(2);
        assertThat(activeBids.getFirst().getBidPrice()).isEqualTo(1500);

        List<BidResponse> cancelledBids = response.getCancelledBids();
        assertThat(cancelledBids).isEmpty();
    }

    @DisplayName("존재 하지 않는 경매 상품에 대한 입찰 정보 조회 시, 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenAuctionDoesNotExist() {
        // given
        User user = createUser("user@test.com", "유저");
        userRepository.save(user);

        Long invalidAuctionId = -1L;

        //when & then
        assertThatThrownBy(() -> bidService.getBidOverview(invalidAuctionId, user))
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessage("해당 경매 상품이 존재하지 않습니다.");
    }

    private Auction createAuctionWithDependencies(int basePrice, LocalDateTime startTime,
                                                  LocalDateTime endTime) {
        Category testCategory = categoryRepository.save(createCategory("test category"));
        User user = userRepository.save(createUser("seller@test.com", "판매자"));
        return createAuction(
                "test auction",
                basePrice,
                testCategory,
                user,
                startTime,
                endTime
        );
    }

    private static Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }


    private User createUser(String email, String name) {
        return User.builder()
                .email(email)
                .name(name)
                .profileImage(email + name)
                .providerId(name)
                .provider("LOCAL")
                .build();

    }

    public Auction createAuction(String title,
                                 Integer basePrice,
                                 Category category,
                                 User seller,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime
    ) {

        return Auction.builder()
                .title(title)
                .description("팔아요!")
                .basePrice(basePrice)
                .itemCondition(ItemCondition.brand_new)
                .startTime(startTime)
                .endTime(endTime)
                .category(category)
                .seller(seller)
                .status(AuctionStatus.pending)
                .build();
    }

    private Bid createBid(Auction auction, User bidder, int price, boolean isWinning, boolean isDeleted) {
        return Bid.builder()
                .bidder(bidder)
                .auction(auction)
                .price(price)
                .isWinning(isWinning)
                .isDeleted(isDeleted)
                .build();
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        int length = name.length();

        if (length == 2) {
            return name.charAt(0) + "*";
        } else if (length >= 3) {
            StringBuilder masked = new StringBuilder();
            masked.append(name.charAt(0));
            masked.append("*".repeat(length - 2));
            masked.append(name.charAt(length - 1));
            return masked.toString();
        }

        return name;
    }

}
