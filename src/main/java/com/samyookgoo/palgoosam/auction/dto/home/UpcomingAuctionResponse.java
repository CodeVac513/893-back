package com.samyookgoo.palgoosam.auction.dto.home;

import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpcomingAuctionResponse {
    private Long auctionId;
    private String title;
    private String description;
    private ItemCondition itemCondition;
    private Integer basePrice;
    private Integer scrapCount;
    private String thumbnailUrl;
    private String leftTime;
}
