package com.samyookgoo.palgoosam.auction.dto.response;

import com.samyookgoo.palgoosam.auction.dto.AuctionSearchResultDto;
import com.samyookgoo.palgoosam.search.domain.AuctionSearchDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuctionSearchDocumentResponseDto {
    private Long totalAuctionsCount;

    private List<AuctionSearchDocument> auctionList;
}
