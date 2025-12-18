package com.samyookgoo.palgoosam.auction.domain;

import com.samyookgoo.palgoosam.auction.constant.AuctionStatus;
import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import com.samyookgoo.palgoosam.auction.dto.request.AuctionCreateRequest;
import com.samyookgoo.palgoosam.bid.exception.BidBadRequestException;
import com.samyookgoo.palgoosam.bid.exception.BidForbiddenException;
import com.samyookgoo.palgoosam.bid.exception.BidInvalidStateException;
import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.user.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auction")
@Entity
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCondition itemCondition;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'pending'")
    @Column(nullable = false, length = 20)
    private AuctionStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionImage> auctionImages = new ArrayList<>();

    @Version
    private Long version;

    public boolean isAuctionOpen(LocalDateTime now) {
        return !now.isBefore(this.startTime) && !now.isAfter(this.endTime);
    }

    public void validateBidConditions(Long bidderId, int price, LocalDateTime now) {
        if (seller.getId().equals(bidderId)) {
            throw new BidForbiddenException(ErrorCode.SELLER_CANNOT_BID);
        }

        if (!isAuctionOpen(now)) {
            throw new BidInvalidStateException(ErrorCode.BID_TIME_INVALID);
        }

        if (price < basePrice) {
            throw new BidBadRequestException(ErrorCode.BID_LESS_THAN_BASE);
        }
    }

    public static Auction from(AuctionCreateRequest request,
                               Category category,
                               User seller,
                               LocalDateTime startTime,
                               LocalDateTime endTime
    ) {
        return Auction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .itemCondition(request.getItemCondition())
                .startTime(startTime)
                .endTime(endTime)
                .category(category)
                .seller(seller)
                .status(AuctionStatus.pending)
                .build();
    }
}
