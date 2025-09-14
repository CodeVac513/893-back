package com.samyookgoo.palgoosam.search.repository;

import com.samyookgoo.palgoosam.search.domain.AuctionSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AuctionSearchElasticsearchRepository extends ElasticsearchRepository<AuctionSearchDocument, String> {

}
