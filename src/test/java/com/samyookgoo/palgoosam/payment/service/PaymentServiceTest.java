package com.samyookgoo.palgoosam.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.exception.AuctionNotFoundException;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.auction.repository.CategoryRepository;
import com.samyookgoo.palgoosam.bid.domain.Bid;
import com.samyookgoo.palgoosam.bid.exception.BidNotFoundException;
import com.samyookgoo.palgoosam.bid.repository.BidRepository;
import com.samyookgoo.palgoosam.payment.constant.PaymentStatus;
import com.samyookgoo.palgoosam.payment.constant.PaymentType;
import com.samyookgoo.palgoosam.payment.controller.request.PaymentCreateRequest;
import com.samyookgoo.palgoosam.payment.controller.request.TossPaymentConfirmRequest;
import com.samyookgoo.palgoosam.payment.controller.request.TossPaymentFailCallbackRequest;
import com.samyookgoo.palgoosam.payment.controller.response.PaymentCreateResponse;
import com.samyookgoo.palgoosam.payment.controller.response.TossPaymentConfirmResponse;
import com.samyookgoo.palgoosam.payment.domain.Payment;
import com.samyookgoo.palgoosam.payment.exception.PaymentBadRequestException;
import com.samyookgoo.palgoosam.payment.exception.PaymentExternalException;
import com.samyookgoo.palgoosam.payment.exception.PaymentForbiddenException;
import com.samyookgoo.palgoosam.payment.exception.PaymentInvalidStateException;
import com.samyookgoo.palgoosam.payment.policy.DeliveryPolicy;
import com.samyookgoo.palgoosam.payment.repository.PaymentRepository;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("test")
@SpringBootTest
class PaymentServiceTest {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private RestTemplate tossRestTemplate;

    @Autowired
    private DeliveryPolicy deliveryPolicy;

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAllInBatch();
        bidRepository.deleteAllInBatch();
        auctionRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("구매자가 결제 버튼 클릭 시, 주문이 생성 되며 결제 대기 상태가 된다.")
    @Test
    void shouldCreatePaymentWhenValidRequest() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        PaymentCreateRequest request = createPaymentRequest(10000, 2500);

        //when
        PaymentCreateResponse response = paymentService.createPayment(auction.getId(), buyer, request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getOrderName()).isEqualTo(auction.getTitle());
        assertThat(response.getCustomerEmail()).isEqualTo(buyer.getEmail());
        assertThat(response.getCustomerName()).isEqualTo(request.getRecipientName());

        Payment savedPayment = paymentRepository.findByOrderNumber(response.getOrderId()).orElseThrow();
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(savedPayment.getOrderNumber()).isEqualTo(response.getOrderId());
    }

    @DisplayName("구매자가 결제 금액을 임의로 조작하면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenPaymentAmountMismatch() {
        //given
        Auction auction = createAuctionWithDependencies(1000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        PaymentCreateRequest request = createPaymentRequest(9999, 2500);

        //when & then
        assertThatThrownBy(() -> paymentService.createPayment(auction.getId(), buyer, request))
                .isInstanceOf(PaymentBadRequestException.class)
                .hasMessage("결제 금액이 낙찰 금액과 일치하지 않습니다.");
    }

    @DisplayName("구매자가 아닌 사용자가 결제를 시도하면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenUserIsNotWinningBidder() {
        //given
        Auction auction = createAuctionWithDependencies(1000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        User anotherUser = createUser("other@test.com", "홍길동");
        userRepository.save(anotherUser);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        PaymentCreateRequest request = createPaymentRequest(10000, 2500);

        //when & then
        assertThatThrownBy(() -> paymentService.createPayment(auction.getId(), anotherUser, request))
                .isInstanceOf(PaymentForbiddenException.class)
                .hasMessageContaining("낙찰자만 결제할 수 있습니다.");
    }

    @DisplayName("존재하지 않는 경매에 대해 결제를 시도하면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenAuctionDoesNotExist() {
        //given
        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        PaymentCreateRequest request = createPaymentRequest(10000, 2500);

        //when & then
        assertThatThrownBy(() -> paymentService.createPayment(-1L, buyer, request))
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessage("해당 경매 상품이 존재하지 않습니다.");
    }

    @DisplayName("낙찰된 입찰이 없으면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenWinningBidDoesNotExist() {
        //given
        Auction auction = createAuctionWithDependencies(1000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        PaymentCreateRequest request = createPaymentRequest(10000, 2500);

        //when & then
        assertThatThrownBy(() -> paymentService.createPayment(auction.getId(), buyer, request))
                .isInstanceOf(BidNotFoundException.class)
                .hasMessageContaining("낙찰된 입찰이 존재하지 않습니다.");
    }

    @DisplayName("이미 결제가 완료된 경우 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenPaymentAlreadyPaid() {
        //given
        Auction auction = createAuctionWithDependencies(1000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(auction, buyer, 10000, 2500, PaymentStatus.PAID,
                "ORD-9999", "PAY-9999", LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentCreateRequest request = createPaymentRequest(10000, 2500);

        //when & then
        assertThatThrownBy(() -> paymentService.createPayment(auction.getId(), buyer, request))
                .isInstanceOf(PaymentInvalidStateException.class)
                .hasMessageContaining("이미 결제 중이거나 완료된 주문입니다.");
    }

    @DisplayName("배송비가 일치하지 않으면 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenDeliveryFeeIsInvalid() {
        //given
        Auction auction = createAuctionWithDependencies(1000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        PaymentCreateRequest request = createPaymentRequest(10000, 9999);

        //when & then
        assertThatThrownBy(() -> paymentService.createPayment(auction.getId(), buyer, request))
                .isInstanceOf(PaymentBadRequestException.class)
                .hasMessageContaining("배송비가 올바르지 않습니다.");
    }

    @DisplayName("이미 결제 준비 상태인 주문이 있으면 해당 주문을 반환한다.")
    @Test
    void shouldReturnExistingPaymentWhenReadyPaymentExists() {
        //given
        Auction auction = createAuctionWithDependencies(1000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(
                auction,
                buyer,
                10000,
                2500,
                PaymentStatus.READY,
                "ORD-9999",
                null,
                null
        );

        paymentRepository.save(payment);

        PaymentCreateRequest request = createPaymentRequest(10000, 2500);

        //when
        PaymentCreateResponse response = paymentService.createPayment(auction.getId(), buyer, request);

        //then
        assertThat(response.getOrderId()).isEqualTo(payment.getOrderNumber());
    }


    @DisplayName("결제 실패 시 결제 상태를 변경한다.")
    @Test
    void shouldMarkPaymentAsFailedWhenPaymentFailureReceived() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 1500, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(auction, buyer, 10000, 2500, PaymentStatus.READY,
                "ORD-9999", null, null);
        paymentRepository.save(payment);

        TossPaymentFailCallbackRequest request = TossPaymentFailCallbackRequest.builder()
                .orderNumber(payment.getOrderNumber())
                .build();

        // when
        paymentService.failPayment(request);

        // then
        Payment updatedPayment = paymentRepository.findByOrderNumber(payment.getOrderNumber()).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @DisplayName("결제 승인 요청시 토스페이먼츠에 최종 결제 승인을 요청하고 성공 시, 결제 정보를 반환한다.")
    @Test
    void shouldMarkPaymentAsPaidWhenPaymentConfirmationReceived() {
        LocalDateTime now = LocalDateTime.now();
        // given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(auction, buyer, 10000, 2500, PaymentStatus.READY,
                "ORD-9999", null, null);
        paymentRepository.save(payment);

        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
                .orderId(payment.getOrderNumber())
                .amount(payment.getFinalPrice())
                .paymentType(PaymentType.CREDIT_CARD)
                .paymentKey("PAY-9999")
                .build();

        TossPaymentConfirmResponse tossResponse = TossPaymentConfirmResponse.builder()
                .orderId(payment.getOrderNumber())
                .approvedAt(OffsetDateTime.now().toString())
                .build();

        given(tossRestTemplate.postForObject(anyString(), eq(request), eq(TossPaymentConfirmResponse.class)))
                .willReturn(tossResponse);

        // when
        TossPaymentConfirmResponse response = paymentService.confirmPayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(request.getOrderId());
        assertThat(response.getCustomerEmail()).isEqualTo(payment.getRecipientEmail());

        Payment updatedPayment = paymentRepository.findByOrderNumber(payment.getOrderNumber()).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(updatedPayment.getPaymentKey()).isEqualTo(request.getPaymentKey());
    }

    @DisplayName("토스페이먼츠 응답이 null인 경우 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenTossResponseIsNull() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(auction, buyer, 10000, 2500, PaymentStatus.READY,
                "ORD-9999", null, null);
        paymentRepository.save(payment);

        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
                .orderId(payment.getOrderNumber())
                .amount(payment.getFinalPrice())
                .paymentType(PaymentType.CREDIT_CARD)
                .paymentKey("PAY-9999")
                .build();

        given(tossRestTemplate.postForObject(anyString(), eq(request), eq(TossPaymentConfirmResponse.class)))
                .willReturn(null);

        //when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
                .isInstanceOf(PaymentExternalException.class)
                .hasMessageContaining("Toss 응답이 비어 있습니다.");
    }

    @DisplayName("토스 결제 승인 요청시 HttpClientErrorException 발생 시 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenHttpClientErrorExceptionOccurs() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(auction, buyer, 10000, 2500, PaymentStatus.READY,
                "ORD-9999", null, null);
        paymentRepository.save(payment);

        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
                .orderId(payment.getOrderNumber())
                .amount(payment.getFinalPrice())
                .paymentType(PaymentType.CREDIT_CARD)
                .paymentKey("PAY-9999")
                .build();

        String errorJson = "{\"code\":\"INVALID_REQUEST\",\"message\":\"유효하지 않은 요청입니다.\"}";
        HttpClientErrorException exception = HttpClientErrorException.BadRequest.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, errorJson.getBytes(), null);

        given(tossRestTemplate.postForObject(anyString(), eq(request), eq(TossPaymentConfirmResponse.class)))
                .willThrow(exception);

        //when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
                .isInstanceOf(PaymentExternalException.class)
                .hasMessage("Toss 결제 오류가 발생했습니다.");
    }

    @DisplayName("토스 결제 오류 응답 파싱 실패 시 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenTossErrorResponseParsingFails() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(auction, buyer, 10000, 2500, PaymentStatus.READY,
                "ORD-9999", null, null);
        paymentRepository.save(payment);

        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
                .orderId(payment.getOrderNumber())
                .amount(payment.getFinalPrice())
                .paymentType(PaymentType.CREDIT_CARD)
                .paymentKey("PAY-9999")
                .build();

        String malformedJson = "{ this is not a valid json }";
        HttpClientErrorException exception = HttpClientErrorException.BadRequest.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, malformedJson.getBytes(), null);

        given(tossRestTemplate.postForObject(anyString(), eq(request), eq(TossPaymentConfirmResponse.class)))
                .willThrow(exception);

        //when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
                .isInstanceOf(PaymentExternalException.class)
                .hasMessage("Toss 결제 오류가 발생했습니다.");
    }

    @DisplayName("토스 결제 처리 중 알 수 없는 예외 발생 시 예외가 발생한다.")
    @Test
    void shouldThrowExceptionWhenUnexpectedExceptionOccurs() {
        LocalDateTime now = LocalDateTime.now();
        //given
        Auction auction = createAuctionWithDependencies(1000, now, now.plusHours(1));
        auctionRepository.save(auction);

        User buyer = createUser("buyer@test.com", "구매자");
        userRepository.save(buyer);

        Bid winningBid = createBid(auction, buyer, 10000, true, false);
        bidRepository.save(winningBid);

        Payment payment = createPayment(auction, buyer, 10000, 2500, PaymentStatus.READY,
                "ORD-9999", null, null);
        paymentRepository.save(payment);

        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
                .orderId(payment.getOrderNumber())
                .amount(payment.getFinalPrice())
                .paymentType(PaymentType.CREDIT_CARD)
                .paymentKey("PAY-9999")
                .build();

        given(tossRestTemplate.postForObject(anyString(), eq(request), eq(TossPaymentConfirmResponse.class)))
                .willThrow(new RuntimeException("Unexpected error"));

        //when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
                .isInstanceOf(PaymentExternalException.class)
                .hasMessage("Toss 결제 오류가 발생했습니다.");
    }

    private Payment createPayment(Auction auction, User buyer, int itemPrice, int deliveryFee,
                                  PaymentStatus status, String orderNumber, String paymentKey,
                                  LocalDateTime approvedAt) {
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
                .itemPrice(itemPrice)
                .deliveryFee(deliveryFee)
                .finalPrice(itemPrice + deliveryFee)
                .orderNumber(orderNumber)
                .paymentKey(paymentKey)
                .status(status)
                .approvedAt(approvedAt)
                .build();
    }

    private PaymentCreateRequest createPaymentRequest(int itemPrice, int deliveryFee) {
        return PaymentCreateRequest.builder()
                .itemPrice(itemPrice)
                .deliveryFee(deliveryFee)
                .recipientName("구매자")
                .phoneNumber("010-0000-0000")
                .addressLine1("서울시")
                .addressLine2("강남구")
                .zipCode("12345")
                .successUrl("http://success")
                .failUrl("http://fail")
                .build();
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
}