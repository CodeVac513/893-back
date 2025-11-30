package com.samyookgoo.palgoosam.auction.dto.request;

import com.samyookgoo.palgoosam.auction.constant.ItemCondition;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuctionUpdateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Min(0)
    private Integer basePrice;

    @NotNull
    private ItemCondition itemCondition;

    @NotNull(message = "경매 오픈 시간은 필수입니다.")
    @Min(0)
    @Max(1440)
    private Integer startDelay;

    @NotNull(message = "경매 소요 시간은 필수입니다.")
    @Min(10)
    @Max(1440)
    private Integer durationTime;

    @NotNull
    private CategoryRequest category;

    private List<AuctionImageRequest> images;
}
