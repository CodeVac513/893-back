package com.samyookgoo.palgoosam.bid.domain;

import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.bid.exception.BidForbiddenException;
import com.samyookgoo.palgoosam.bid.exception.BidInvalidStateException;
import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.payment.exception.PaymentBadRequestException;
import com.samyookgoo.palgoosam.payment.exception.PaymentForbiddenException;
import com.samyookgoo.palgoosam.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "bid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    private int price;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @ColumnDefault("false")
    private Boolean isWinning = false;

    @Builder.Default
    @ColumnDefault("false")
    private Boolean isDeleted = false;

    public boolean isOwner(Long userId) {
        return bidder.getId().equals(userId);
    }

    public boolean isCancelled() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public void cancel() {
        this.setIsWinning(false);
        this.setIsDeleted(true);
    }

    public void validateCancelConditions(Long userId, LocalDateTime now) {
        if (!auction.isAuctionOpen(now)) {
            throw new BidInvalidStateException(ErrorCode.BID_TIME_INVALID);
        }
        if (!isOwner(userId)) {
            throw new BidForbiddenException(ErrorCode.BID_CANCEL_FORBIDDEN);
        }
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new BidInvalidStateException(ErrorCode.BID_ALREADY_CANCELED);
        }
        if (now.isAfter(this.createdAt.plusMinutes(1))) {
            throw new BidInvalidStateException(ErrorCode.BID_CANCEL_EXPIRED);
        }
    }

    public void validatePaymentConditions(Long buyerId, Integer amount) {
        if (!this.bidder.getId().equals(buyerId)) {
            throw new PaymentForbiddenException(ErrorCode.NOT_WINNING_BIDDER);
        }

        if (this.price != amount) {
            throw new PaymentBadRequestException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    public static Bid placeBy(Auction auction, User user, int price) {
        return Bid.builder()
                .auction(auction)
                .bidder(user)
                .price(price)
                .isWinning(true)
                .isDeleted(false)
                .build();
    }
}
