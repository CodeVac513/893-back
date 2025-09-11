//package com.samyookgoo.palgoosam.auction.repository;
//
//import static com.samyookgoo.palgoosam.auction.constant.AuctionStatus.completed;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
//import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
//import com.samyookgoo.palgoosam.auction.domain.Auction;
//import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
//import com.samyookgoo.palgoosam.auction.domain.Category;
//import com.samyookgoo.palgoosam.auction.projection.AuctionBidCount;
//import com.samyookgoo.palgoosam.auction.projection.TopWinningBid;
//import com.samyookgoo.palgoosam.bid.domain.Bid;
//import com.samyookgoo.palgoosam.bid.repository.BidRepository;
//import com.samyookgoo.palgoosam.user.domain.User;
//import com.samyookgoo.palgoosam.user.repository.UserRepository;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.context.ActiveProfiles;
//
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ActiveProfiles("test")
//@DataJpaTest
//class BidRepositoryTest {
//
//    @Autowired
//    private BidRepository bidRepository;
//
//    @Autowired
//    private AuctionRepository auctionRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private AuctionImageRepository auctionImageRepository;
//
//
//    @DisplayName("최근 7일 내 완료된 경매 중 낙찰가 기준 TOP 5를 조회한다")
//    @Test
//    void testFindTop5WinningBids() {
//        // given
//        LocalDateTime now = LocalDateTime.now();
//        Category category = categoryRepository.save(createCategory("전자기기", null));
//        User seller = userRepository.save(createUser("seller@test.com", "판매자"));
//        User buyer = userRepository.save(createUser("buyer@test.com", "구매자"));
//
//        for (int i = 0; i < 6; i++) {
//            Auction auction = createAuction(category, seller,
//                    now.minusDays(2), now.minusDays(1), completed, now.minusDays(3));
//            auctionRepository.save(auction);
//            auctionImageRepository.save(createAuctionImage(auction, "img" + i));
//
//            // 낙찰자 1명 (가격 점점 높게)
//            bidRepository.save(Bid.builder()
//                    .auction(auction)
//                    .price(1000 + i * 500)
//                    .bidder(buyer)
//                    .isWinning(true)
//                    .createdAt(now.minusDays(1))
//                    .build());
//        }
//
//        // when
////        List<TopWinningBid> result = bidRepository.findTop5WinningBids(now.minusDays(7), PageRequest.of(0, 5));
//        List<TopWinningBid> result = bidRepository.findTop5WinningBids();
//
//        // then
//        assertThat(result).hasSize(5);
//        assertThat(result.get(0).getItemPrice()).isGreaterThan(result.get(1).getItemPrice());
//    }
//
//    @DisplayName("경매 ID 목록을 기반으로 입찰 수를 집계한다")
//    @Test
//    void testCountBidsByAuctionIds() {
//        // given
//        LocalDateTime now = LocalDateTime.now();
//        User user = userRepository.save(createUser("user@test.com", "사용자"));
//        Category category = categoryRepository.save(createCategory("전자기기", null));
//        Auction auction1 = createAuction(category, user, now.minusHours(2), now.minusHours(1), completed, now);
//        Auction auction2 = createAuction(category, user, now.minusHours(3), now.minusHours(2), completed, now);
//        auctionRepository.saveAll(List.of(auction1, auction2));
//
//        for (int i = 0; i < 3; i++) {
//            bidRepository.save(createBid(auction1, user, 1000 + i * 10));
//        }
//
//        for (int i = 0; i < 2; i++) {
//            bidRepository.save(createBid(auction2, user, 1200 + i * 20));
//        }
//
//        // when
//        List<AuctionBidCount> result = bidRepository.countBidsByAuctionIds(List.of(auction1.getId(), auction2.getId()));
//
//        // then
//        Map<Long, Integer> map = result.stream()
//                .collect(Collectors.toMap(AuctionBidCount::getAuctionId, AuctionBidCount::getBidCount));
//
//        assertThat(map.get(auction1.getId())).isEqualTo(3);
//        assertThat(map.get(auction2.getId())).isEqualTo(2);
//    }
//
//
//    private Category createCategory(String name, Category parent) {
//        return categoryRepository.save(
//                Category.builder()
//                        .name(name)
//                        .parent(parent)
//                        .build()
//        );
//    }
//
//    private User createUser(String email, String name) {
//        return userRepository.save(
//                User.builder()
//                        .email(email)
//                        .name(name)
//                        .profileImage("img.png")
//                        .providerId(name)
//                        .provider("LOCAL")
//                        .build()
//        );
//    }
//
//    private Auction createAuction(Category category, User seller,
//                                  LocalDateTime startTime, LocalDateTime endTime,
//                                  AuctionStatus status, LocalDateTime createdAt) {
//        return auctionRepository.save(
//                Auction.builder()
//                        .title("상품 제목")
//                        .description("상품 내용")
//                        .basePrice(100000)
//                        .category(category)
//                        .seller(seller)
//                        .startTime(startTime)
//                        .endTime(endTime)
//                        .status(status)
//                        .itemCondition(ItemCondition.brand_new)
//                        .createdAt(createdAt)
//                        .build()
//        );
//    }
//
//    private AuctionImage createAuctionImage(Auction auction, String url) {
//        return auctionImageRepository.save(
//                AuctionImage.builder()
//                        .auction(auction)
//                        .url(url)
//                        .originalName("origin" + url)
//                        .storeName("store" + url)
//                        .imageSeq(0)
//                        .isDeleted(false)
//                        .build()
//        );
//    }
//
//    private Bid createBid(Auction auction, User bidder, int price) {
//        return Bid.builder()
//                .auction(auction)
//                .price(price)
//                .bidder(bidder)
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//}
