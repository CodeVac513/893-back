package com.samyookgoo.palgoosam.user.unit_test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.domain.AuctionForMyPageProjection;
import com.samyookgoo.palgoosam.bid.domain.BidForMyPageProjection;
import com.samyookgoo.palgoosam.payment.domain.PaymentForMyPageProjection;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.dto.UserAuctionsResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserBidsResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserInfoResponseDto;
import com.samyookgoo.palgoosam.user.dto.UserPaymentsResponseDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DisplayName("User 도메인의 응답 DTO 유닛 테스트")
class UserResponseDtoTest {

    //UserAuctionResponseDto 정적 팩토리 메서드
    @Test
    @DisplayName("UserAuctionResponseDto를 정적 팩토리 메서드를 통해 생성할 수 있다.")
    public void Given_AuctionForMyPageProjectionAndHighestBid_When_CallOfMethod_Then_ReturnResponseDto() {
        //given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = now.plusMinutes(10);

        AuctionForMyPageProjection projection = mock(AuctionForMyPageProjection.class);
        when(projection.getAuctionId()).thenReturn(1L);
        when(projection.getTitle()).thenReturn("테스트 경매");
        when(projection.getStartTime()).thenReturn(now);
        when(projection.getEndTime()).thenReturn(endAt);
        when(projection.getStatus()).thenReturn(AuctionStatus.active);
        when(projection.getMainImageUrl()).thenReturn("test.jpg");

        Integer highestBid = 1500;
        //when
        UserAuctionsResponseDto createdDto = UserAuctionsResponseDto.of(projection, highestBid);

        //then
        assertThat(createdDto.getAuctionId()).isEqualTo(1L);
        assertThat(createdDto.getTitle()).isEqualTo("테스트 경매");
        assertThat(createdDto.getStartTime()).isEqualTo(now);
        assertThat(createdDto.getEndTime()).isEqualTo(endAt);
        assertThat(createdDto.getStatus()).isEqualTo(AuctionStatus.active.toString());
        assertThat(createdDto.getMainImageUrl()).isEqualTo("test.jpg");
        assertThat(createdDto.getBidHighestPrice()).isEqualTo(highestBid);
    }

    //UserBidsResponseDto 정적 팩토리 메서드
    @Test
    @DisplayName("UserBidsResponseDto를 정적 팩토리 메서드를 통해 생성할 수 있다.")
    public void Given_BidForMyPageProjectionAndHighestBid_When_CallOfMethod_Then_ReturnResponseDto() {
        //given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = now.plusMinutes(10);

        BidForMyPageProjection projection = mock(BidForMyPageProjection.class);
        when(projection.getAuctionId()).thenReturn(1L);
        when(projection.getBidId()).thenReturn(1L);
        when(projection.getIsWinning()).thenReturn(false);
        when(projection.getUserPrice()).thenReturn(1111);
        when(projection.getTitle()).thenReturn("테스트 경매");
        when(projection.getStartTime()).thenReturn(now);
        when(projection.getEndTime()).thenReturn(endAt);
        when(projection.getStatus()).thenReturn(AuctionStatus.active);
        when(projection.getMainImageUrl()).thenReturn("test.jpg");

        Integer highestBid = 1500;
        //when
        UserBidsResponseDto createdDto = UserBidsResponseDto.of(projection, highestBid);

        //then
        assertThat(createdDto.getAuctionId()).isEqualTo(1L);
        assertThat(createdDto.getBidId()).isEqualTo(1L);
        assertThat(createdDto.getIsWinning()).isEqualTo(false);
        assertThat(createdDto.getUserPrice()).isEqualTo(1111);
        assertThat(createdDto.getTitle()).isEqualTo("테스트 경매");
        assertThat(createdDto.getMainImageUrl()).isEqualTo("test.jpg");
        assertThat(createdDto.getStartTime()).isEqualTo(now);
        assertThat(createdDto.getEndTime()).isEqualTo(endAt);
        assertThat(createdDto.getStatus()).isEqualTo(AuctionStatus.active.toString());
        assertThat(createdDto.getBidHighestPrice()).isEqualTo(highestBid);
    }

    //UserBidsResponseDto 정적 팩토리 메서드
    @Test
    @DisplayName("UserInfoResponseDto를 정적 팩토리 메서드를 통해 생성할 수 있다.")
    public void Given_User_When_CallFromMethod_Then_ReturnResponseDto() {
        //given
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("tester")
                .profileImage("test@cdn.com")
                .provider("google")
                .providerId("google-test")
                .build();

        //when
        UserInfoResponseDto createdDto = UserInfoResponseDto.from(user);

        //then
        assertThat(createdDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(createdDto.getName()).isEqualTo(user.getName());
        assertThat(createdDto.getProfileUrl()).isEqualTo(user.getProfileImage());
        assertThat(createdDto.getProvider()).isEqualTo(user.getProvider());
    }

    //UserBidsResponseDto 정적 팩토리 메서드
    @Test
    @DisplayName("UserPaymentsResponseDto를 정적 팩토리 메서드를 통해 생성할 수 있다.")
    public void Given_PaymentForMyPageProjection_When_CallOfMethod_Then_ReturnResponseDto() {
        //given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = now.plusMinutes(10);

        PaymentForMyPageProjection projection = mock(PaymentForMyPageProjection.class);
        when(projection.getAuctionId()).thenReturn(1L);
        when(projection.getOrderNumber()).thenReturn("toss-test");
        when(projection.getTitle()).thenReturn("테스트 경매");
        when(projection.getMainImageUrl()).thenReturn("test.jpg");
        when(projection.getFinalPrice()).thenReturn(1500);

        //when
        UserPaymentsResponseDto createdDto = UserPaymentsResponseDto.of(projection);

        //then
        assertThat(createdDto.getAuctionId()).isEqualTo(1L);
        assertThat(createdDto.getOrderNumber()).isEqualTo("toss-test");
        assertThat(createdDto.getFinalPrice()).isEqualTo(1500);
        assertThat(createdDto.getTitle()).isEqualTo("테스트 경매");
        assertThat(createdDto.getMainImageUrl()).isEqualTo("test.jpg");
    }
}
