package com.samyookgoo.palgoosam.auction.event;

import com.samyookgoo.palgoosam.search.domain.AuctionSearchDocument;

public class ElasticsearchAuctionEvent {
    private final AuctionSearchDocument searchDocument;

    public ElasticsearchAuctionEvent(AuctionSearchDocument searchDocument) {
        this.searchDocument = searchDocument;
    }

    public AuctionSearchDocument getSearchDocument() {
        return searchDocument;
    }}
