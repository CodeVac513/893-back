package com.samyookgoo.palgoosam.auction.event_listener;

import com.samyookgoo.palgoosam.auction.domain.AuctionOutbox;
import com.samyookgoo.palgoosam.auction.event.ElasticsearchAuctionEvent;
import com.samyookgoo.palgoosam.auction.repository.AuctionOutboxRepository;
import com.samyookgoo.palgoosam.search.domain.AuctionSearchDocument;
import com.samyookgoo.palgoosam.search.repository.AuctionSearchElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
public class AuctionEventListener {

    private final AuctionSearchElasticsearchRepository auctionSearchElasticsearchRepository;
    private final AuctionOutboxRepository auctionOutboxRepository;
    private final String AGGREGATE_TYPE = "auction";

    @Async
    @Transactional(propagation = REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuctionCreated(ElasticsearchAuctionEvent event) {
        AuctionSearchDocument saved = auctionSearchElasticsearchRepository.save(event.getSearchDocument());
        AuctionOutbox auctionOutbox = auctionOutboxRepository.getAuctionOutboxById(event.getOutboxId());
        auctionOutbox.processEvent();
        auctionOutboxRepository.save(auctionOutbox);
    }
}
