package com.samyookgoo.palgoosam.auction.dto.request;

import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuctionCreateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Min(0)
    @Max(value = 100_000_000, message = "시작가는 최대 1억원까지만 입력 가능합니다.")
    private Integer basePrice;

    @NotNull
    private ItemCondition itemCondition;

    @NotNull
    @Min(0)
    @Max(1440)
    private Integer startDelay;

    @NotNull
    @Min(10)
    @Max(1440)
    private Integer durationTime;

    @NotNull
    private CategoryRequest category;
}