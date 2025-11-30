package com.samyookgoo.palgoosam.bid.controller;

import com.samyookgoo.palgoosam.auth.service.AuthService;
import com.samyookgoo.palgoosam.bid.api_docs.CancelBidApi;
import com.samyookgoo.palgoosam.bid.api_docs.GetAuctionBidsApi;
import com.samyookgoo.palgoosam.bid.api_docs.PlaceBidApi;
import com.samyookgoo.palgoosam.bid.controller.request.BidRequest;
import com.samyookgoo.palgoosam.bid.controller.response.BaseResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidOverviewResponse;
import com.samyookgoo.palgoosam.bid.controller.response.BidResultResponse;
import com.samyookgoo.palgoosam.bid.service.BidService;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Tag(name = "입찰", description = "경매 입찰 관련 API")
public class BidController {

    private final BidService bidService;
    private final AuthService authService;

    @GetAuctionBidsApi
    @GetMapping("/{auctionId}/bids")
    public BaseResponse<BidOverviewResponse> overview(
            @Parameter(name = "auctionId", description = "입찰 내역을 조회할 경매 ID", required = true)
            @PathVariable Long auctionId
    ) {
        User user = authService.getCurrentUser();

        BidOverviewResponse response = bidService.getBidOverview(auctionId, user);
        return BaseResponse.success(response);
    }

    @PlaceBidApi
    @PostMapping("/{auctionId}/bids")
    public BaseResponse<BidResultResponse> place(
            @Parameter(name = "auctionId", description = "입찰할 경매 ID", required = true)
            @PathVariable Long auctionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "입찰 요청 정보 (입찰가 포함)",
                    required = true
            )
            @Valid @RequestBody BidRequest request
    ) {
        User user = authService.getCurrentUser();
        if (user == null) {
            throw new UserNotFoundException();
        }

        BidResultResponse response = bidService.placeBid(auctionId, user, request.getPrice());
        return BaseResponse.success(response);
    }

    @CancelBidApi
    @CrossOrigin(origins = "http://localhost:3000")
    @PatchMapping("/{auctionId}/bids/{bidId}")
    public BaseResponse<String> cancel(
            @Parameter(name = "auctionId", description = "경매 ID", required = true)
            @PathVariable Long auctionId,

            @Parameter(name = "bidId", description = "취소할 입찰 ID", required = true)
            @PathVariable Long bidId
    ) {
        User user = authService.getCurrentUser();
        if (user == null) {
            throw new UserNotFoundException();
        }

        bidService.cancelBid(auctionId, bidId, user.getId(), LocalDateTime.now());

        return BaseResponse.success("입찰 취소 완료");
    }
}
