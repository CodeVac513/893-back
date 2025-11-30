package com.samyookgoo.palgoosam.bid.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.samyookgoo.palgoosam.bid.service.SseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(controllers = SseController.class)
@AutoConfigureMockMvc(addFilters = false)
class SseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SseService sseService;

    @DisplayName("SSE 구독 요청을 수행한다.")
    @Test
    void subscribe() throws Exception {
        // given
        Long auctionId = 1L;
        SseEmitter mockEmitter = new SseEmitter();
        given(sseService.subscribe(eq(auctionId))).willReturn(mockEmitter);

        // when & then
        mockMvc.perform(get("/api/auctions/{auctionId}/stream", auctionId))
                .andDo(print())
                .andExpect(status().isOk());

        // verify
        verify(sseService).subscribe(eq(auctionId));
    }
}