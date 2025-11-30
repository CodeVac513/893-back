package com.samyookgoo.palgoosam.auction.dto.home;

import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingRankingResponse {
    private Long auctionId;
    private String title;
    private String description;
    private ItemCondition itemCondition;
    private String thumbnailUrl;
    private Integer scrapCount;
    private Integer rankNum;
}
