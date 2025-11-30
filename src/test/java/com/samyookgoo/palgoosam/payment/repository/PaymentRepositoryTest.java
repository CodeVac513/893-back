package com.samyookgoo.palgoosam.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.auction.repository.CategoryRepository;
import com.samyookgoo.palgoosam.payment.constant.PaymentStatus;
import com.samyookgoo.palgoosam.payment.domain.Payment;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @DisplayName("경매 ID로 결제 정보를 조회한다.")
    @Test
    void findByAuctionId() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Payment payment = createPayment(auction, buyer, "ORD-123", PaymentStatus.READY);
        paymentRepository.save(payment);

        //when
        Payment result = paymentRepository.findByAuction_Id(auction.getId()).orElseThrow();

        //then
        assertThat(result).isEqualTo(payment);
    }

    @DisplayName("주문 번호로 결제 정보를 조회한다.")
    @Test
    void findByOrderNumber() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Payment payment = createPayment(auction, buyer, "ORD-123", PaymentStatus.READY);
        paymentRepository.save(payment);

        //when
        Payment result = paymentRepository.findByOrderNumber("ORD-123").orElseThrow();

        //then
        assertThat(result).isEqualTo(payment);
    }

    @DisplayName("경매의 결제 상태가 조회 하려는 상태 목록에 포함 되는 지 여부를 확인한다.")
    @Test
    void existsByAuctionIdAndStatusIn() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Payment payment = createPayment(auction, buyer, "ORD-123", PaymentStatus.PAID);
        paymentRepository.save(payment);

        //when
        boolean exists = paymentRepository.existsByAuctionIdAndStatusIn(auction.getId(),
                List.of(PaymentStatus.PAID, PaymentStatus.PENDING));

        //then
        assertThat(exists).isTrue();
    }

    @DisplayName("결제 상태가 특정 상태인지 여부를 확인한다.")
    @Test
    void existsByAuction_IdAndStatus() {
        //given
        Auction auction = createAuctionWithDependencies("test auction", 1000);
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Payment payment = createPayment(auction, buyer, "ORD-123", PaymentStatus.READY);
        paymentRepository.save(payment);

        //when
        boolean exists = paymentRepository.existsByAuction_IdAndStatus(auction.getId(), PaymentStatus.READY);

        //then
        assertThat(exists).isTrue();
    }

    private Auction createAuctionWithDependencies(String title, Integer basePrice) {
        Category category = categoryRepository.save(Category.builder().name("Test Category").build());
        User seller = userRepository.save(createUser("seller@test.com", "판매자"));

        return Auction.builder()
                .title(title)
                .description("팔아요!")
                .basePrice(basePrice)
                .itemCondition(ItemCondition.brand_new)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .category(category)
                .seller(seller)
                .status(AuctionStatus.pending)
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

    private Payment createPayment(Auction auction, User buyer, String orderNumber, PaymentStatus status) {
        return Payment.builder()
                .buyer(buyer)
                .seller(auction.getSeller())
                .auction(auction)
                .recipientName(buyer.getName())
                .recipientEmail(buyer.getEmail())
                .phoneNumber("010-0000-0000")
                .addressLine1("서울시")
                .addressLine2("강남구")
                .zipCode("12345")
                .itemPrice(10000)
                .deliveryFee(2500)
                .finalPrice(12500)
                .orderNumber(orderNumber)
                .status(status)
                .build();
    }
}
