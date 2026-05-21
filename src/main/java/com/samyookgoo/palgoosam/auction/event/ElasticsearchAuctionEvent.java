package com.samyookgoo.palgoosam.auction.event;

import com.samyookgoo.palgoosam.search.domain.AuctionSearchDocument;

public class ElasticsearchAuctionEvent {
    private final AuctionSearchDocument searchDocument;
    private final Long outboxId;

    public ElasticsearchAuctionEvent(AuctionSearchDocument searchDocument, Long outboxId) {
        this.searchDocument = searchDocument;
        this.outboxId = outboxId;
    }

    public AuctionSearchDocument getSearchDocument() {
        return searchDocument;
    }
    public Long getOutboxId() {
        return outboxId;
    }
}
