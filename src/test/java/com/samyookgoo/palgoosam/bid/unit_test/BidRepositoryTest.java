package com.samyookgoo.palgoosam.bid.unit_test;

import static org.assertj.core.api.Assertions.assertThat;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.bid.domain.Bid;
import com.samyookgoo.palgoosam.bid.domain.BidForHighestPriceProjection;
import com.samyookgoo.palgoosam.bid.domain.BidForMyPageProjection;
import com.samyookgoo.palgoosam.bid.repository.BidRepository;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BidRepository 유닛 테스트")
class BidRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    private User bidder;
    private User seller;
    private Auction auction1;
    private Auction auction2;
    private Bid bid1;
    private Bid bid2;
    private Bid bid3;

    @BeforeEach
    void setUp() {
        // 공통 given
        // 더미 카테고리 생성
        Category testCategory = Category.builder()
                .name("Test Category")
                .build();
        Category savedCategory = entityManager.persistAndFlush(testCategory);

        // 사용자 생성
        bidder = createUser("bidder@test.com", "입찰자");
        seller = createUser("seller@test.com", "판매자");

        // 경매 생성
        auction1 = createAuction(seller, "첫번째 경매", 1000, savedCategory);
        auction2 = createAuction(seller, "두번째 경매", 2000, savedCategory);

        // 경매 이미지 생성
        createAuctionImage(auction1, "image1.jpg");
        createAuctionImage(auction2, "image2.jpg");

        // 입찰 생성
        bid1 = createBid(bidder, auction1, 1500, false); // 첫번째 경매에 1500원 입찰
        bid2 = createBid(bidder, auction1, 2000, false); // 첫번째 경매에 2000원 입찰
        bid3 = createBid(bidder, auction2, 2500, false); // 두번째 경매에 2500원 입찰

        // 다른 사용자의 입찰 (최고가 테스트용)
        User otherBidder = createUser("other@test.com", "다른입찰자");
        createBid(otherBidder, auction1, 2200, false); // 첫번째 경매에 더 높은 입찰

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("사용자의 입찰 내역 중 입찰가가 가장 높은 것을 조회한다.")
    void Given_BidderId_When_RetrieveBids_Then_ReturnBidHistory() {
        //when
        List<BidForMyPageProjection> result = bidRepository.findAllBidsByUserId(bidder.getId());

        //then
        assertThat(result).hasSize(2);

        BidForMyPageProjection firstBid = result.stream()
                .filter(bid -> bid.getAuctionId().equals(auction1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstBid.getUserPrice()).isEqualTo(2000);
        assertThat(firstBid.getTitle()).isEqualTo("첫번째 경매");
        assertThat(firstBid.getMainImageUrl()).isEqualTo("image1.jpg");
        assertThat(firstBid.getAuctionId()).isEqualTo(auction1.getId());
        assertThat(firstBid.getStatus()).isEqualTo(AuctionStatus.active);
    }

    @Test
    @DisplayName("사용자가 참여한 경매의 최고 입찰가를 조회한다.")
    public void Given_BidderId_When_RetrieveBids_Then_ReturnHighestBid() {
        //when
        List<BidForHighestPriceProjection> result = bidRepository.findHighestBidProjectsByBidderId(bidder.getId());

        //then
        assertThat(result).hasSize(2);

        BidForHighestPriceProjection firstBid = result.stream()
                .filter(bid -> bid.getAuctionId().equals(auction1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstBid.getAuctionId()).isEqualTo(auction1.getId());
        assertThat(firstBid.getBidHighestPrice()).isEqualTo(2200);

        // 두 번째 경매 입찰 검증
        BidForHighestPriceProjection secondBid = result.stream()
                .filter(bid -> bid.getAuctionId().equals(auction2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(secondBid.getAuctionId()).isEqualTo(auction2.getId());
        assertThat(secondBid.getBidHighestPrice()).isEqualTo(2500);
    }

    @Test
    @DisplayName("사용자가 등록한 경매의 최고 입찰가를 조회한다.")
    public void Given_Seller_When_RetrieveBids_Then_ReturnHighestBid() {
        //when
        List<BidForHighestPriceProjection> result = bidRepository.findHighestBidProjectsBySellerId(seller.getId());

        //then
        assertThat(result).hasSize(2);

        BidForHighestPriceProjection firstBid = result.stream()
                .filter(bid -> bid.getAuctionId().equals(auction1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstBid.getAuctionId()).isEqualTo(auction1.getId());
        assertThat(firstBid.getBidHighestPrice()).isEqualTo(2200);

        // 두 번째 경매 입찰 검증
        BidForHighestPriceProjection secondBid = result.stream()
                .filter(bid -> bid.getAuctionId().equals(auction2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(secondBid.getAuctionId()).isEqualTo(auction2.getId());
        assertThat(secondBid.getBidHighestPrice()).isEqualTo(2500);
    }

    @Test
    @DisplayName("사용자가 등록한 경매의 입찰이 없으면 0을 반환한다.")
    public void Given_Seller_When_RetrieveBids_Then_ReturnZero() {
        //given
        Category testCategory = Category.builder()
                .name("Test Category2")
                .build();
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        Auction auctionWithoutBid = createAuction(seller, "auctionWithoutBid", 1111, savedCategory);

        //when
        List<BidForHighestPriceProjection> result = bidRepository.findHighestBidProjectsBySellerId(seller.getId());

        //then
        assertThat(result).hasSize(3);

        BidForHighestPriceProjection firstBid = result.stream()
                .filter(bid -> bid.getAuctionId().equals(auctionWithoutBid.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstBid.getAuctionId()).isEqualTo(auctionWithoutBid.getId());
        assertThat(firstBid.getBidHighestPrice()).isEqualTo(0);
    }


    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .profileImage(email + name)
                .providerId(name)
                .provider("LOCAL")
                .build();
        return entityManager.persistAndFlush(user);
    }

    private Auction createAuction(User seller, String title, int basePrice, Category category) {
        Auction auction = Auction.builder()
                .seller(seller)
                .title(title)
                .description("경매 설명")
                .basePrice(basePrice)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .status(AuctionStatus.active)
                .category(category)
                .itemCondition(ItemCondition.damaged)
                .build();
        return entityManager.persistAndFlush(auction);
    }

    private void createAuctionImage(Auction auction, String url) {
        AuctionImage image = AuctionImage.builder()
                .auction(auction)
                .url(url)
                .originalName("test-original-" + url) // 원본 파일명 추가
                .storeName("test-store-" + url)       // 저장된 파일명 추가
                .imageSeq(0) // 메인 이미지
                .isDeleted(false) // 명시적으로 설정
                .build();
        entityManager.persistAndFlush(image);
    }

    private Bid createBid(User bidder, Auction auction, int price, boolean isWinning) {
        Bid bid = Bid.builder()
                .bidder(bidder)
                .auction(auction)
                .price(price)
                .isWinning(isWinning)
                .isDeleted(false)
                .build();
        return entityManager.persistAndFlush(bid);
    }
}