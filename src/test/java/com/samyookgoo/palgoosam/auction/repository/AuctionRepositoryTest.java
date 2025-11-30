package com.samyookgoo.palgoosam.auction.repository;

import static com.samyookgoo.palgoosam.auction.constant.AuctionStatus.active;
import static com.samyookgoo.palgoosam.auction.constant.AuctionStatus.completed;
import static com.samyookgoo.palgoosam.auction.constant.AuctionStatus.pending;
import static org.assertj.core.api.Assertions.assertThat;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.projection.AuctionBidCount;
import com.samyookgoo.palgoosam.auction.projection.AuctionScrapCount;
import com.samyookgoo.palgoosam.auction.projection.DashboardProjection;
import com.samyookgoo.palgoosam.auction.projection.RankingAuction;
import com.samyookgoo.palgoosam.auction.projection.RecentAuction;
import com.samyookgoo.palgoosam.auction.projection.SubCategoryBestItem;
import com.samyookgoo.palgoosam.auction.projection.UpcomingAuction;
import com.samyookgoo.palgoosam.bid.domain.Bid;
import com.samyookgoo.palgoosam.bid.repository.BidRepository;
import com.samyookgoo.palgoosam.user.domain.Scrap;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.repository.ScrapRepository;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest
class AuctionRepositoryTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionImageRepository auctionImageRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ScrapRepository scrapRepository;

    @DisplayName("총 이용자 수, 총 등록된 경매 수, 현재 진행중인 경매 수 조회한다")
    @Test
    void testGetDashboardCounts() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = categoryRepository.save(createCategory("전자기기", null));
        User user = userRepository.save(createUser("test@test.com", "사용자"));

        // 경매 3건 저장 (2개는 active, 1개는 completed)
        auctionRepository.save(
                createAuction(category, user, now.minusHours(1), now.plusHours(1), active, now.minusHours(2)));
        auctionRepository.save(
                createAuction(category, user, now.minusHours(2), now.plusHours(2), active, now.minusHours(2)));
        auctionRepository.save(
                createAuction(category, user, now.minusHours(2), now.minusHours(1), completed, now.minusHours(2)));

        // when
        DashboardProjection result = auctionRepository.getDashboardCounts();

        // then
        assertThat(result.getTotalUserCount()).isEqualTo(1);
        assertThat(result.getTotalAuctionCount()).isEqualTo(3);
        assertThat(result.getActiveAuctionCount()).isEqualTo(2);
    }

    @DisplayName("최근 등록된 경매 6개를 조회한다")
    @Test
    void testFindTop6RecentAuctions() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = categoryRepository.save(createCategory("전자기기", null));
        User seller = userRepository.save(createUser("seller@test.com", "판매자"));

        for (int i = 0; i < 8; i++) {
            Auction auction = createAuction(category, seller, now.minusMinutes(i + 1), now.plusHours(1),
                    active, now.minusMinutes(i));
            auctionRepository.save(auction);
            auctionImageRepository.save(createAuctionImage(auction, "image" + i + "url"));
        }

        // when
        List<RecentAuction> result = auctionRepository.findTop6RecentAuctions(
                List.of(active, pending),
                PageRequest.of(0, 6)
        );

        // then
        assertThat(result).hasSize(6);
        assertThat(result.get(0).getThumbnailUrl()).isEqualTo("image7url");
        assertThat(result.get(0).getAuctionId()).isNotNull();
    }

    @DisplayName("시작 예정인 경매 3개를 조회한다")
    @Test
    void testFindTop3UpcomingAuctions() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = categoryRepository.save(createCategory("전자기기", null));
        User seller = userRepository.save(createUser("seller@test.com", "판매자"));

        for (int i = 0; i < 5; i++) {
            Auction auction = createAuction(category, seller,
                    now.plusMinutes(10 + i), now.plusHours(2),
                    AuctionStatus.pending, now.minusHours(1));
            auctionRepository.save(auction);
            auctionImageRepository.save(createAuctionImage(auction, "image" + i));
        }

        // when
        List<UpcomingAuction> result = auctionRepository.findTop3UpcomingAuctions(
                AuctionStatus.pending, PageRequest.of(0, 3));

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStartTime()).isBefore(result.get(1).getStartTime());
        assertThat(result.get(0).getThumbnailUrl()).contains("image");
    }

    @DisplayName("경매 진행중인 상품 입찰 수 기준 내림차순, 입찰 수 동일하면 auctionId 오름차순 정렬 조회")
    @Test
    void testFindTop8AuctionBidCounts() {
        // given
        Category category = categoryRepository.save(createCategory("전자기기", null));
        User seller = userRepository.save(createUser("seller@test.com", "판매자"));
        User bidder = userRepository.save(createUser("bidder@test.com", "입찰자"));

        LocalDateTime now = LocalDateTime.now();

        Auction auction1 = createAuction(category, seller, now.minusHours(3), now.plusHours(1), active,
                now.minusHours(3));
        Auction auction2 = createAuction(category, seller, now.minusHours(2), now.plusHours(2), active,
                now.minusHours(2));
        Auction auction3 = createAuction(category, seller, now.minusHours(1), now.plusHours(3), active,
                now.minusHours(1));

        bidRepository.save(createBid(auction1, bidder, 1000));
        bidRepository.save(createBid(auction1, bidder, 2000));
        bidRepository.save(createBid(auction2, bidder, 1500));
        bidRepository.save(createBid(auction2, bidder, 2500));
        bidRepository.save(createBid(auction3, bidder, 1300));

        // when
        Pageable pageable = PageRequest.of(0, 8);
        List<AuctionBidCount> results = auctionRepository.findTop8AuctionBidCounts(pageable);

        // then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getAuctionId()).isEqualTo(Math.min(auction1.getId(), auction2.getId()));
        assertThat(results.get(1).getAuctionId()).isEqualTo(Math.max(auction1.getId(), auction2.getId()));
        assertThat(results.get(2).getAuctionId()).isEqualTo(auction3.getId());
    }

    @DisplayName("경매 ID 목록에 해당하는 RankingAuction 정보를 조회한다.")
    @Test
    void testFindRankingByIds() {
        // given
        Category category = categoryRepository.save(createCategory("전자기기", null));
        User seller = userRepository.save(createUser("seller@test.com", "판매자"));

        Auction auction1 = auctionRepository.save(createAuction(category, seller,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                AuctionStatus.active,
                LocalDateTime.now().minusDays(1)
        ));

        Auction auction2 = auctionRepository.save(createAuction(category, seller,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(5),
                AuctionStatus.active,
                LocalDateTime.now().minusHours(3)
        ));

        auctionImageRepository.save(createAuctionImage(auction1, "image1"));
        auctionImageRepository.save(createAuctionImage(auction2, "image2"));
        auctionImageRepository.save(createAuctionImage(auction2, "image3")); // imageSeq != 0

        List<Long> auctionIds = List.of(auction1.getId(), auction2.getId());

        // when
        List<RankingAuction> result = auctionRepository.findRankingByIds(auctionIds);

        // then
        assertThat(result).hasSize(3);

        RankingAuction r1 = result.stream()
                .filter(r -> r.getAuctionId().equals(auction1.getId()))
                .findFirst().orElseThrow();

        assertThat(r1.getTitle()).isEqualTo(auction1.getTitle());
        assertThat(r1.getThumbnailUrl()).isEqualTo("image1");

        RankingAuction r2 = result.stream()
                .filter(r -> r.getAuctionId().equals(auction2.getId()))
                .findFirst().orElseThrow();

        assertThat(r2.getTitle()).isEqualTo(auction2.getTitle());
        assertThat(r2.getThumbnailUrl()).isEqualTo("image2"); // imageSeq == 0
    }

    @Test
    @DisplayName("스크랩 수 기준으로 대기 중인 경매 TOP 8을 조회한다.")
    void testFindTop8AuctionScrapCounts() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = categoryRepository.save(createCategory("전자기기", null));
        User seller = userRepository.save(createUser("seller@test.com", "판매자"));
        User scrapper = userRepository.save(createUser("scrapper@test.com", "스크래퍼"));

        for (int i = 0; i < 10; i++) {
            Auction auction = createAuction(category, seller, now.plusHours(i), now.plusHours(i + 1), pending,
                    now.minusHours(i));
            auctionRepository.save(auction);

            for (int j = 0; j < i; j++) {
                scrapRepository.save(createScrap(scrapper, auction));
            }
        }

        // when
        Pageable pageable = PageRequest.of(0, 8);
        List<AuctionScrapCount> result = auctionRepository.findTop8AuctionScrapCounts(pageable);

        // then
        assertThat(result).hasSize(8);
        assertThat(result.get(0).getScrapCount()).isGreaterThanOrEqualTo(result.get(1).getScrapCount());
    }

//    @DisplayName("서브카테고리 ID로 상위 50개 경매 조회")
//    @Test
//    void testFindTop50BySubCategoryId() {
//        // given
//        LocalDateTime now = LocalDateTime.now();
//        Category mainCategory = categoryRepository.save(createCategory("전자기기", null));
//        Category subCategory = categoryRepository.save(createCategory("모바일", mainCategory));
//        User seller = userRepository.save(createUser("seller@test.com", "판매자"));
//
//        for (int i = 0; i < 50; i++) {
//            Auction auction = createAuction(subCategory, seller, now.plusMinutes(i), now.plusMinutes(i + 10), pending,
//                    now.minusMinutes(i));
//
//            auctionRepository.save(auction);
//            auctionImageRepository.save(createAuctionImage(auction, "image" + i));
//        }
//
//        Pageable pageable = PageRequest.of(0, 50);
//
//        // when
//        List<SubCategoryBestItem> topItems = auctionRepository.findTop50BySubCategoryId(subCategory.getId(), pageable);
//
//        // then
//        assertThat(topItems).hasSize(50);
//        assertThat(topItems.get(0).getAuctionId()).isNotNull();
//    }

    private Category createCategory(String name, Category parent) {
        return categoryRepository.save(
                Category.builder()
                        .name(name)
                        .parent(parent)
                        .build()
        );
    }

    private User createUser(String email, String name) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .name(name)
                        .profileImage("img.png")
                        .providerId(name)
                        .provider("LOCAL")
                        .build()
        );
    }

    private Auction createAuction(Category category, User seller,
                                  LocalDateTime startTime, LocalDateTime endTime,
                                  AuctionStatus status, LocalDateTime createdAt) {
        return auctionRepository.save(
                Auction.builder()
                        .title("상품 제목")
                        .description("상품 내용")
                        .basePrice(100000)
                        .category(category)
                        .seller(seller)
                        .startTime(startTime)
                        .endTime(endTime)
                        .status(status)
                        .itemCondition(ItemCondition.brand_new)
                        .createdAt(createdAt)
                        .build()
        );
    }

    private AuctionImage createAuctionImage(Auction auction, String url) {
        return auctionImageRepository.save(
                AuctionImage.builder()
                        .auction(auction)
                        .url(url)
                        .originalName("origin" + url)
                        .storeName("store" + url)
                        .imageSeq(0)
                        .isDeleted(false)
                        .build()
        );
    }

    private Bid createBid(Auction auction, User bidder, int price) {
        return Bid.builder()
                .auction(auction)
                .price(price)
                .bidder(bidder)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Scrap createScrap(User user, Auction auction) {
        return Scrap.builder()
                .user(user)
                .auction(auction)
                .build();
    }

}