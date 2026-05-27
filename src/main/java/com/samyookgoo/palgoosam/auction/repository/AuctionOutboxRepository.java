package com.samyookgoo.palgoosam.auction.repository;

import com.samyookgoo.palgoosam.auction.constant.EventStatus;
import com.samyookgoo.palgoosam.auction.domain.AuctionOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionOutboxRepository extends JpaRepository<AuctionOutbox, Long> {
    AuctionOutbox getAuctionOutboxById(Long id);

    List<AuctionOutbox> findByEventStatusAndRetryCountLessThan(EventStatus eventStatus, Long maxRetryCount);
}
