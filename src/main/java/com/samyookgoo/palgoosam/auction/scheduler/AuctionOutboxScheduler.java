package com.samyookgoo.palgoosam.auction.scheduler;

import com.samyookgoo.palgoosam.auction.constant.EventStatus;
import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
import com.samyookgoo.palgoosam.auction.domain.AuctionOutbox;
import com.samyookgoo.palgoosam.auction.repository.AuctionImageRepository;
import com.samyookgoo.palgoosam.auction.repository.AuctionOutboxRepository;
import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import com.samyookgoo.palgoosam.auction.service.CategoryService;
import com.samyookgoo.palgoosam.search.domain.AuctionSearchDocument;
import com.samyookgoo.palgoosam.search.repository.AuctionSearchElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionOutboxScheduler {

    private static final Long MAX_RETRY_COUNT = 5L;

    private final AuctionOutboxRepository auctionOutboxRepository;
    private final AuctionRepository auctionRepository;
    private final CategoryService categoryService;
    private final AuctionImageRepository auctionImageRepository;
    private final AuctionSearchElasticsearchRepository auctionSearchElasticsearchRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processAuctionCreationEvent() {
        List<AuctionOutbox> pendingOutboxes = auctionOutboxRepository
                .findByEventStatusAndRetryCountLessThan(EventStatus.PENDING, MAX_RETRY_COUNT);

        for (AuctionOutbox auctionOutbox : pendingOutboxes) {
            try {
                Auction auction = auctionRepository
                        .findById(auctionOutbox.getAuctionId())
                        .orElseThrow();

                List<Long> ancestorIds = categoryService
                        .collectAncestorIds(auction.getCategory());

                List<AuctionImage> images = auctionImageRepository
                        .findByAuctionId(auctionOutbox.getAuctionId());

                AuctionSearchDocument searchDocument = new AuctionSearchDocument(
                        auction.getId().toString(),
                        auction.getTitle(),
                        auction.getDescription(),
                        ancestorIds,
                        auction.getItemCondition().toString(),
                        auction.getStatus().toString(),
                        auction.getBasePrice(),
                        auction.getBasePrice(),
                        0L,
                        0L,
                        auction.getCreatedAt(),
                        auction.getStartTime(),
                        auction.getEndTime(),
                        images.getFirst().getUrl()
                );

                auctionSearchElasticsearchRepository.save(searchDocument);
                auctionOutbox.processEvent();
            } catch (Exception e) {
                auctionOutbox.incrementRetryCount();
            } finally {
                auctionOutboxRepository.save(auctionOutbox);
            }
        }
    }
}
