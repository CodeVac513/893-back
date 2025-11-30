package com.samyookgoo.palgoosam.auction.dto.home;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecentAuctionResponse {
    private Long auctionId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private AuctionStatus status;
    private Integer basePrice;
}
