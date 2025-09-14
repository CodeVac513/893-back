package com.samyookgoo.palgoosam.search.domain;

import com.samyookgoo.palgoosam.auction.domain.Category;
import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Document(indexName = "auction")
@Setting(settingPath = "/elasticsearch/auction-settings.json")
public class AuctionSearchDocument {

    @Id
    private String id;

    // 검색 대상 필드
    @Field(type = FieldType.Text, analyzer = "auction_title_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "auction_description_analyzer")
    private String description;

    // 필터링 필드
    @Field(name = "category_id", type = FieldType.Long)
    private List<Long> categoryId;

    @Field(name = "item_condition", type = FieldType.Keyword)
    private String itemCondition;  // brand_new, like_new 등

    @Field(type = FieldType.Keyword)
    private String status;  // pending, active, completed

    @Field(name = "base_price", type = FieldType.Integer)
    private Integer basePrice;

    @Field(name = "current_price", type = FieldType.Integer)
    private Integer currentPrice;  // 현재가 (입찰 있으면 최고가, 없으면 basePrice)

    // 정렬용 필드
    @Field(name = "bidder_count", type = FieldType.Long)
    private Long bidderCount;

    @Field(name = "scrap_count", type = FieldType.Long)
    private Long scrapCount;

    @Field(name = "created_at", type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(name = "start_time", type = FieldType.Date)
    private LocalDateTime startTime;

    @Field(name = "end_time", type = FieldType.Date)
    private LocalDateTime endTime;

    // 검색 결과 표시용
    @Field(name = "thumbnail_url", type = FieldType.Keyword, index = false)  // 검색 안함
    private String thumbnailUrl;
}
