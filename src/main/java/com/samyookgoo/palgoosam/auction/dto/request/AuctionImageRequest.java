package com.samyookgoo.palgoosam.auction.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuctionImageRequest {
    private Long imageId;   // 기존 이미지면 ID 존재, 새 이미지면 null
    private Integer imageSeq;   // 이미지 순서 (0이 대표 이미지)
}