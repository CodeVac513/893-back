package com.samyookgoo.palgoosam.auction.dto.response;

import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "경매 이미지 응답 DTO")
public class AuctionImageResponse {

    @Schema(description = "이미지 ID", example = "101")
    private Long imageId;

    @Schema(description = "업로드된 이미지의 원본 파일명", example = "iphone.jpg")
    private String originalName;

    @Schema(description = "서버에 저장된 파일명", example = "f3a1c2e3-iphone.jpg")
    private String storeName;

    @Schema(description = "이미지 접근 URL", example = "https://cdn.893.com/images/f3a1c2e3-iphone.jpg")
    private String url;

    @Schema(description = "이미지 순서 (0이 대표 이미지)", example = "0")
    private Integer imageSeq;

    public static AuctionImageResponse from(AuctionImage image) {
        return AuctionImageResponse.builder()
                .imageId(image.getId())
                .originalName(image.getOriginalName())
                .storeName(image.getStoreName())
                .url(image.getUrl())
                .imageSeq(image.getImageSeq())
                .build();
    }
}
