package com.samyookgoo.palgoosam.auction.event_listener;

import com.samyookgoo.palgoosam.auction.event.ElasticsearchAuctionEvent;
import com.samyookgoo.palgoosam.search.repository.AuctionSearchElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuctionEventListener {

    private final AuctionSearchElasticsearchRepository auctionSearchElasticsearchRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuctionCreated(ElasticsearchAuctionEvent event) {
        auctionSearchElasticsearchRepository.save(event.getSearchDocument());
    }
}
