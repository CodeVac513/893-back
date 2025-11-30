package com.samyookgoo.palgoosam.bid.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.repository.AuctionImageRepository;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.auction.repository.CategoryRepository;
import com.samyookgoo.palgoosam.bid.domain.Bid;
import com.samyookgoo.palgoosam.bid.service.response.BidStatsResponse;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class BidRepositoryTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuctionRepository auctionRepository;

    @Autowired
    AuctionImageRepository auctionImageRepository;

    @Autowired
    private BidRepository bidRepository;

    @DisplayName("경매의 입찰 내역을 생성일 기준 내림차순으로 모두 가져온다.")
    @Test
    void findByAuctionIdOrderByCreatedAtDesc() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        AuctionImage auctionImage = createAuctionImage(auction, "img.jpg");
        auctionRepository.save(auction);
        auctionImageRepository.save(auctionImage);

        User user = createUser("user1@test.com", "유저1");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, false, false);
        Bid bid2 = createBid(auction, user, 2000, false, true);
        Bid bid3 = createBid(auction, user, 2500, true, false);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        //when
        List<Bid> result = bidRepository.findByAuctionIdOrderByCreatedAtDesc(auction.getId());

        //then
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(bid3);
        assertThat(result.get(1)).isEqualTo(bid2);
        assertThat(result.get(2)).isEqualTo(bid1);

    }

    @DisplayName("경매의 취소되지 않은 입찰 내역 중 최고 입찰가를 반환한다.")
    @Test
    void findMaxBidPriceByAuctionId() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        AuctionImage auctionImage = createAuctionImage(auction, "img.jpg");
        auctionRepository.save(auction);
        auctionImageRepository.save(auctionImage);

        User user = createUser("user1@test.com", "유저1");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, false, false);
        Bid bid2 = createBid(auction, user, 2000, true, false);
        Bid bid3 = createBid(auction, user, 2500, false, true);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        //when
        Integer maxPrice = bidRepository.findMaxBidPriceByAuctionId(auction.getId());

        //then
        assertThat(maxPrice).isEqualTo(2000);
    }

    @DisplayName("경매의 유효한 입찰 중 최고 입찰을 반환한다.")
    @Test
    void findTopValidBidByAuctionId() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        AuctionImage auctionImage = createAuctionImage(auction, "img.jpg");
        auctionRepository.save(auction);
        auctionImageRepository.save(auctionImage);

        User user = createUser("user1@test.com", "유저1");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, false, false);
        Bid bid2 = createBid(auction, user, 2000, true, false);
        Bid bid3 = createBid(auction, user, 2500, false, true);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        //when
        Optional<Bid> result = bidRepository.findTopValidBidByAuctionId(auction.getId());

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualTo(2000);
    }

    @DisplayName("경매의 낙찰된 입찰을 단건 조회한다.")
    @Test
    void findByAuctionIdAndIsWinningTrue() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        AuctionImage auctionImage = createAuctionImage(auction, "img.jpg");

        auctionRepository.save(auction);
        auctionImageRepository.save(auctionImage);

        User user = createUser("user1@test.com", "유저1");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, false, false);
        Bid bid2 = createBid(auction, user, 2000, true, false);
        Bid bid3 = createBid(auction, user, 2500, false, true);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        //when
        Optional<Bid> result = bidRepository.findByAuctionIdAndIsWinningTrue(auction.getId());

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualTo(2000);
    }

    @DisplayName("경매의 유효한 입찰 수를 조회한다.")
    @Test
    void countByAuctionIdAndIsDeletedFalse() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        AuctionImage auctionImage = createAuctionImage(auction, "img.jpg");
        auctionRepository.save(auction);
        auctionImageRepository.save(auctionImage);

        User user = createUser("user1@test.com", "유저1");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, false, false);
        Bid bid2 = createBid(auction, user, 2000, true, false);
        Bid bid3 = createBid(auction, user, 2500, false, true);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        //when
        Integer count = bidRepository.countByAuctionIdAndIsDeletedFalse(auction.getId());

        //then
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("유저가 해당 경매에서 취소한 입찰이 있는 지 확인한다.")
    @Test
    void existsByAuctionIdAndBidderIdAndIsDeletedTrue() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        AuctionImage auctionImage = createAuctionImage(auction, "img.jpg");
        auctionRepository.save(auction);
        auctionImageRepository.save(auctionImage);

        User user = createUser("user1@test.com", "유저1");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, false, false);
        Bid bid2 = createBid(auction, user, 2000, true, false);
        Bid bid3 = createBid(auction, user, 2500, false, true);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        //when
        Boolean exists = bidRepository.existsByAuctionIdAndBidderIdAndIsDeletedTrue(auction.getId(), user.getId());

        //then
        assertThat(exists).isEqualTo(true);
    }

    @DisplayName("경매의 입찰 통계(최고가, 입찰 수, 입찰자 수)를 조회한다.")
    @Test
    void findBidStatsByAuctionId() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        AuctionImage auctionImage = createAuctionImage(auction, "img.jpg");
        auctionRepository.save(auction);
        auctionImageRepository.save(auctionImage);

        User user = createUser("user1@test.com", "유저1");
        userRepository.save(user);

        Bid bid1 = createBid(auction, user, 1500, false, false);
        Bid bid2 = createBid(auction, user, 2000, true, false);
        Bid bid3 = createBid(auction, user, 2500, false, true);
        bidRepository.saveAll(List.of(bid1, bid2, bid3));

        //when
        BidStatsResponse stats = bidRepository.findBidStatsByAuctionId(auction.getId());

        //then
        assertThat(stats).isNotNull();
        assertThat(stats.getMaxPrice()).isEqualTo(2000);
        assertThat(stats.getTotalBid()).isEqualTo(2);
        assertThat(stats.getTotalBidder()).isEqualTo(1);
    }

    private Auction createAuctionWithDependencies(String auctionTitle, int basePrice) {
        Category testCategory = categoryRepository.save(createCategory("test category"));
        User user = userRepository.save(createUser("seller@test.com", "판매자"));
        return createAuction(
                auctionTitle,
                basePrice,
                testCategory,
                user,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
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


    private AuctionImage createAuctionImage(Auction auction, String url) {
        return AuctionImage.builder()
                .auction(auction)
                .url(url)
                .originalName("test-original-" + url)
                .storeName("test-store-" + url)
                .imageSeq(0)
                .isDeleted(false)
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

}