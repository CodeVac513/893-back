package com.samyookgoo.palgoosam.auction.domain;

import com.samyookgoo.palgoosam.auction.constant.EventStatus;
import com.samyookgoo.palgoosam.auction.constant.EventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auction_outbox")
@Entity
public class AuctionOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "auction_id", nullable = false)
    private Long auctionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 50)
    private EventStatus eventStatus;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Long retryCount = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public AuctionOutbox(String aggregateType, Long auctionId, EventType eventType, EventStatus eventStatus) {
        this.aggregateType = aggregateType;
        this.auctionId = auctionId;
        this.eventType = eventType;
        this.eventStatus = eventStatus;
        this.retryCount = 0L;
    }

    public void processEvent() {
        this.eventStatus = EventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    private static final Long MAX_RETRY_COUNT = 5L;

    public void incrementRetryCount() {
        this.retryCount++;
        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.eventStatus = EventStatus.DEAD;
        }
    }
}
