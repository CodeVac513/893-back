package com.samyookgoo.palgoosam.bid.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samyookgoo.palgoosam.auth.service.AuthService;
import com.samyookgoo.palgoosam.bid.controller.request.BidRequest;
import com.samyookgoo.palgoosam.bid.controller.response.BidOverviewResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidResultResponse;
import com.samyookgoo.palgoosam.bid.service.BidService;
import com.samyookgoo.palgoosam.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BidController.class)
@AutoConfigureMockMvc(addFilters = false)
class BidControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private BidService bidService;

    @MockBean
    private AuthService authService;

    @DisplayName("입찰 내역을 조회한다.")
    @Test
    void overview() throws Exception {
        // given
        Long auctionId = 1L;
        BidOverviewResponse response = BidOverviewResponse.builder().build();

        given(authService.getCurrentUser()).willReturn(null);
        given(bidService.getBidOverview(anyLong(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/auctions/{auctionId}/bids", auctionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @DisplayName("입찰을 등록한다.")
    @Test
    void placeBid() throws Exception {
        // given
        Long auctionId = 1L;
        User mockUser = User.builder().id(100L).build();

        BidRequest request = BidRequest.builder()
                .price(10000)
                .build();

        BidResultResponse response = BidResultResponse.builder().build();

        given(authService.getCurrentUser()).willReturn(mockUser);
        given(bidService.placeBid(anyLong(), any(), anyInt())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auctions/{auctionId}/bids", auctionId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @DisplayName("비회원 유저는 입찰이 불가하다.")
    @Test
    void guestCannotPlaceBid() throws Exception {
        // given
        Long auctionId = 1L;

        BidRequest request = BidRequest.builder()
                .price(10000)
                .build();

        BidResultResponse response = BidResultResponse.builder().build();

        given(authService.getCurrentUser()).willReturn(null);
        given(bidService.placeBid(anyLong(), any(), anyInt())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auctions/{auctionId}/bids", auctionId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("U_001"))
                .andExpect(jsonPath("$.message").value("해당 유저를 찾을 수 없습니다."));
    }

    @DisplayName("입찰을 취소한다.")
    @Test
    void cancelBid() throws Exception {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;
        User mockUser = User.builder().id(100L).build();

        given(authService.getCurrentUser()).willReturn(mockUser);

        // when & then
        mockMvc.perform(patch("/api/auctions/{auctionId}/bids/{bidId}", auctionId, bidId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value("입찰 취소 완료"));
    }

    @DisplayName("비회원은 입찰 취소가 불가하다.")
    @Test
    void guestCannotCancelBid() throws Exception {
        // given
        Long auctionId = 1L;
        Long bidId = 10L;

        given(authService.getCurrentUser()).willReturn(null);

        // when & then
        mockMvc.perform(patch("/api/auctions/{auctionId}/bids/{bidId}", auctionId, bidId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("U_001"))
                .andExpect(jsonPath("$.message").value("해당 유저를 찾을 수 없습니다."));
    }
}
