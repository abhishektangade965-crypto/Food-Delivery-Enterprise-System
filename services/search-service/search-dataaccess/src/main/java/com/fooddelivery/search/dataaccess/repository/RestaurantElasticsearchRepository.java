package com.fooddelivery.search.dataaccess.repository;

import com.fooddelivery.search.dataaccess.document.RestaurantElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantElasticsearchRepository extends ElasticsearchRepository<RestaurantElasticDocument, String> {
}
