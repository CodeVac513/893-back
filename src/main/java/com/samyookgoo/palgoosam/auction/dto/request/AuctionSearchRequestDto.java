package com.samyookgoo.palgoosam.auction.dto.request;

import com.samyookgoo.palgoosam.auction.service.dto.AuctionSearchDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuctionSearchRequestDto {

    private String keyword;

    // 상품 사용감
    private Boolean isBrandNew;
    private Boolean isLikeNew;
    private Boolean isGentlyUsed;
    private Boolean isHeavilyUsed;
    private Boolean isDamaged;

    @Max(1000000000)
    @Min(0)
    private Integer maxPrice;

    @Max(1000000000)
    @Min(0)
    private Integer minPrice;

    // 경매 진행 상태
    private Boolean isPending;
    private Boolean isActive;
    private Boolean isCompleted;

    @NotNull
    private String sortBy = "latest";

    private Long mainCategoryId;
    private Long subCategoryId;
    private Long detailCategoryId;

    @NotNull(message = "페이지는 null이 될 수 없습니다.")
    @Min(value = 1, message = "페이지의 최소 값은 1입니다.")
    private Integer page = 1;

    @NotNull(message = "limit은 null이 될 수 없습니다.")
    @Min(value = 12, message = "limit의 최소 값은 12입니다.")
    private Integer limit = 12;

    public AuctionSearchDto toAuctionSearchDto() {
        Long categoryId = this.checkCategoryId();
        return new AuctionSearchDto(keyword, categoryId, isBrandNew, isLikeNew, isGentlyUsed, isHeavilyUsed,
                isDamaged, minPrice, maxPrice, isPending, isActive, isCompleted, page, limit, sortBy,
                (this.page - 1) * this.limit);
    }

    private Long checkCategoryId() {
        if (this.detailCategoryId != null) {
            return this.detailCategoryId;
        }

        if (this.subCategoryId != null) {
            return this.subCategoryId;
        }

        if (this.mainCategoryId != null) {
            return this.mainCategoryId;
        }

        return null;
    }
}
