package com.samyookgoo.palgoosam.auction.event;

import com.samyookgoo.palgoosam.auction.constant.EventType;

public class ElasticsearchAuctionOutboxEvent {
    private Long auctionId;
    private EventType eventType;

    public ElasticsearchAuctionOutboxEvent(Long auctionId, EventType eventType) {
        this.auctionId = auctionId;
        this.eventType = eventType;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public EventType getEventType() {
        return eventType;
    }
}
