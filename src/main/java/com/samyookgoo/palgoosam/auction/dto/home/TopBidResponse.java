package com.samyookgoo.palgoosam.auction.dto.home;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopBidResponse {
    private Long auctionId;
    private String title;
    private Integer bidCount;
    private Integer basePrice;
    private Integer itemPrice;
    private String thumbnailUrl;
    private String buyer;
    private Integer rankNum;
}
