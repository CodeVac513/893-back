package com.samyookgoo.palgoosam.search.domain;

import com.samyookgoo.palgoosam.auction.domain.Category;
import jakarta.persistence.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "auction")
public class AuctionSearchDocument {
    @Id
    private String id;

    // 검색 대상 필드
    @Field(type = FieldType.Text, analyzer = "korean")
    private String title;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String description;

    // 필터링 필드
    @Field(type = FieldType.Long)
    private List<Long> categoryId;

    @Field(type = FieldType.Keyword)  // exact match용
    private String itemCondition;  // brand_new, like_new 등

    @Field(type = FieldType.Keyword)
    private String status;  // pending, active, completed

    @Field(type = FieldType.Integer)
    private Integer basePrice;

    @Field(type = FieldType.Integer)
    private Integer currentPrice;  // 현재가 (입찰 있으면 최고가, 없으면 basePrice)

    // 정렬용 필드
    @Field(type = FieldType.Long)
    private Long bidderCount;

    @Field(type = FieldType.Long)
    private Long scrapCount;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime startTime;

    @Field(type = FieldType.Date)
    private LocalDateTime endTime;

    // 검색 결과 표시용
    @Field(type = FieldType.Keyword, index = false)  // 검색 안함
    private String thumbnailUrl;
}
