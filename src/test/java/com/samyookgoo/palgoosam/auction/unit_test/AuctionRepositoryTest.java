package com.samyookgoo.palgoosam.auction.unit_test;

import static org.assertj.core.api.Assertions.assertThat;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.AuctionForMyPageProjection;
import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.bid.repository.BidRepository;
import com.samyookgoo.palgoosam.user.domain.Scrap;
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
@DisplayName("AuctionRepository 유닛 테스트")
class AuctionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    private User seller;
    private User scraper;
    private Auction auction1;
    private Auction auction2;
    private Auction auction3;
    private Scrap scrap1;
    private Scrap scrap2;
    private Scrap scrap3;

    @BeforeEach
    void setUp() {
        // 공통 given
        // 더미 카테고리 생성
        Category testCategory = Category.builder()
                .name("Test Category")
                .build();
        Category savedCategory = entityManager.persistAndFlush(testCategory);

        // 사용자 생성
        seller = createUser("seller@test.com", "판매자");
        scraper = createUser("scraper@test.com", "스크랩 사용자");

        // 경매 생성
        auction1 = createAuction(seller, "첫번째 경매", 1000, savedCategory);
        auction2 = createAuction(seller, "두번째 경매", 2000, savedCategory);
        auction3 = createAuction(seller, "세번째 경매", 3000, savedCategory);

        // 경매 이미지 생성
        createAuctionImage(auction1, "image1.jpg");
        createAuctionImage(auction2, "image2.jpg");
        createAuctionImage(auction3, "image3.jpg");

        // 스크랩 생성
        scrap1 = createScrap(scraper, auction1);
        scrap2 = createScrap(scraper, auction2);
        scrap3 = createScrap(scraper, auction3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("사용자가 등록한 모든 경매를 조회한다.")
    void findAllAuctionProjectionBySellerId_ValidSeller_ReturnsAuctions() {
        //when
        List<AuctionForMyPageProjection> result = auctionRepository.findAllAuctionProjectionBySellerId(seller.getId());

        //then
        assertThat(result).hasSize(3);

        // 첫 번째 경매
        AuctionForMyPageProjection firstAuction = result.stream()
                .filter(auction -> auction.getAuctionId().equals(auction1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstAuction.getAuctionId()).isEqualTo(auction1.getId());
        assertThat(firstAuction.getTitle()).isEqualTo("첫번째 경매");
        assertThat(firstAuction.getMainImageUrl()).isEqualTo("image1.jpg");

        // 두 번째 경매
        AuctionForMyPageProjection secondAuction = result.stream()
                .filter(auction -> auction.getAuctionId().equals(auction2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(secondAuction.getAuctionId()).isEqualTo(auction2.getId());
        assertThat(secondAuction.getTitle()).isEqualTo("두번째 경매");
        assertThat(secondAuction.getMainImageUrl()).isEqualTo("image2.jpg");

        // 세 번째 경매
        AuctionForMyPageProjection thirdAuction = result.stream()
                .filter(auction -> auction.getAuctionId().equals(auction3.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(thirdAuction.getAuctionId()).isEqualTo(auction3.getId());
        assertThat(thirdAuction.getTitle()).isEqualTo("세번째 경매");
        assertThat(thirdAuction.getMainImageUrl()).isEqualTo("image3.jpg");
    }


    @Test
    @DisplayName("사용자가 스크랩한 모든 경매를 조회한다.")
    void findAllAuctionProjectionWithScrapByUserId_ValidUser_ReturnsScrapedAuctions() {
        //when
        List<AuctionForMyPageProjection> result = auctionRepository.findAllAuctionProjectionWithScrapByUserId(
                scraper.getId());

        //then
        assertThat(result).hasSize(3);

        // 첫 번째 경매
        AuctionForMyPageProjection firstAuction = result.stream()
                .filter(auction -> auction.getAuctionId().equals(auction1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstAuction.getAuctionId()).isEqualTo(auction1.getId());
        assertThat(firstAuction.getTitle()).isEqualTo("첫번째 경매");
        assertThat(firstAuction.getMainImageUrl()).isEqualTo("image1.jpg");

        // 두 번째 경매
        AuctionForMyPageProjection secondAuction = result.stream()
                .filter(auction -> auction.getAuctionId().equals(auction2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(secondAuction.getAuctionId()).isEqualTo(auction2.getId());
        assertThat(secondAuction.getTitle()).isEqualTo("두번째 경매");
        assertThat(secondAuction.getMainImageUrl()).isEqualTo("image2.jpg");

        // 세 번째 경매
        AuctionForMyPageProjection thirdAuction = result.stream()
                .filter(auction -> auction.getAuctionId().equals(auction3.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(thirdAuction.getAuctionId()).isEqualTo(auction3.getId());
        assertThat(thirdAuction.getTitle()).isEqualTo("세번째 경매");
        assertThat(thirdAuction.getMainImageUrl()).isEqualTo("image3.jpg");
    }

    @Test
    @DisplayName("사용자가 등록한 경매가 없다면 아무 것도 없는 빈 값을 조회한다.")
    void findAllAuctionProjectionBySellerId_NoAuctions_ReturnsEmptyList() {
        //when
        List<AuctionForMyPageProjection> result = auctionRepository.findAllAuctionProjectionBySellerId(scraper.getId());

        //then
        assertThat(result).hasSize(0);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자가 스크랩한 경매가 없다면 아무 것도 없는 빈 값을 조회한다.")
    void findAllAuctionProjectionWithScrapByUserId_NoScraps_ReturnsEmptyList() {
        //when
        List<AuctionForMyPageProjection> result = auctionRepository.findAllAuctionProjectionWithScrapByUserId(
                seller.getId());

        //then
        assertThat(result).hasSize(0);
        assertThat(result).isEmpty();
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

    private Scrap createScrap(User scraper, Auction auction) {
        Scrap scrap = Scrap.builder()
                .user(scraper)
                .auction(auction)
                .build();
        return entityManager.persistAndFlush(scrap);
    }
}