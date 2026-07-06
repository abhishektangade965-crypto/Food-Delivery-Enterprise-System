package com.fooddelivery.search.dataaccess.adapter;

import com.fooddelivery.search.dataaccess.document.RestaurantElasticDocument;
import com.fooddelivery.search.dataaccess.repository.RestaurantElasticsearchRepository;
import com.fooddelivery.search.domain.model.RestaurantSearchModel;
import com.fooddelivery.search.domain.model.RestaurantSearchResult;
import com.fooddelivery.search.domain.port.output.repository.RestaurantSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantSearchRepositoryImpl implements RestaurantSearchRepository {

    private final RestaurantElasticsearchRepository elasticsearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void save(RestaurantSearchModel restaurant) {
        log.info("Indexing restaurant with id: {}", restaurant.getId());
        elasticsearchRepository.save(toDocument(restaurant));
    }

    @Override
    public void deleteById(String id) {
        log.info("Deleting restaurant from index with id: {}", id);
        elasticsearchRepository.deleteById(id);
    }

    @Override
    public RestaurantSearchResult search(
            String query,
            Double latitude,
            Double longitude,
            List<String> cuisines,
            Boolean vegOnly,
            String priceRange,
            Double minRating,
            String sortBy,
            int page,
            int size
    ) {
        log.info("Executing elasticsearch search: query={}, lat={}, lon={}, cuisines={}, vegOnly={}, price={}, minRating={}, sortBy={}",
                query, latitude, longitude, cuisines, vegOnly, priceRange, minRating, sortBy);

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 1. Build Query (BoolQuery)
        BoolQuery.Builder bool = new BoolQuery.Builder();

        // Fuzzy search on name and description
        if (query != null && !query.isBlank()) {
            bool.must(m -> m.bool(b -> b
                .should(s -> s.match(mt -> mt.field("name").query(query).fuzziness("AUTO")))
                .should(s -> s.match(mt -> mt.field("description").query(query).fuzziness("AUTO")))
                .should(s -> s.match(mt -> mt.field("tags").query(query).fuzziness("AUTO")))
            ));
        }

        // Cuisines filter
        if (cuisines != null && !cuisines.isEmpty()) {
            bool.filter(f -> f.terms(t -> t
                .field("cuisineTypes")
                .terms(v -> v.value(cuisines.stream().map(co.elastic.clients.elasticsearch._types.FieldValue::of).collect(Collectors.toList())))
            ));
        }

        // Veg-only filter
        if (Boolean.TRUE.equals(vegOnly)) {
            bool.filter(f -> f.term(t -> t.field("isVeg").value(true)));
        }

        // Price range filter
        if (priceRange != null && !priceRange.isBlank()) {
            bool.filter(f -> f.term(t -> t.field("priceRange").value(priceRange)));
        }

        // Min rating filter
        if (minRating != null) {
            bool.filter(f -> f.range(r -> r.field("rating").gte(co.elastic.clients.json.JsonData.of(minRating))));
        }

        // Geo-location filter: let's filter restaurants within a reasonable distance (e.g. 50km) if latitude/longitude provided
        if (latitude != null && longitude != null) {
            bool.filter(f -> f.geoDistance(gd -> gd
                .field("location")
                .distance("50km")
                .location(l -> l.latlon(ll -> ll.lat(latitude).lon(longitude)))
            ));
        }

        queryBuilder.withQuery(new Query(bool.build()));

        // 2. Sorting
        if (sortBy != null) {
            if ("distance".equalsIgnoreCase(sortBy) && latitude != null && longitude != null) {
                queryBuilder.withSort(SortOptions.of(s -> s.geoDistance(gd -> gd
                    .field("location")
                    .location(l -> l.latlon(ll -> ll.lat(latitude).lon(longitude)))
                    .order(SortOrder.Asc)
                )));
            } else if ("rating".equalsIgnoreCase(sortBy)) {
                queryBuilder.withSort(SortOptions.of(s -> s.field(f -> f.field("rating").order(SortOrder.Desc))));
            } else {
                queryBuilder.withSort(SortOptions.of(s -> s.field(f -> f.field("_score").order(SortOrder.Desc))));
            }
        } else {
            // Default sort: featured first, then score
            queryBuilder.withSort(SortOptions.of(s -> s.field(f -> f.field("featured").order(SortOrder.Desc))));
            queryBuilder.withSort(SortOptions.of(s -> s.field(f -> f.field("_score").order(SortOrder.Desc))));
        }

        // 3. Facets / Aggregations
        queryBuilder.withAggregation("cuisine_types", Aggregation.of(a -> a
            .terms(t -> t.field("cuisineTypes").size(10))
        ));
        queryBuilder.withAggregation("price_ranges", Aggregation.of(a -> a
            .terms(t -> t.field("priceRange").size(10))
        ));

        // 4. Execute Search
        NativeQuery nativeQuery = queryBuilder.build();
        SearchHits<RestaurantElasticDocument> searchHits = elasticsearchOperations.search(nativeQuery, RestaurantElasticDocument.class);

        // Map Results
        List<RestaurantSearchModel> restaurants = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toModel)
                .collect(Collectors.toList());

        // Extract Aggregations
        Map<String, Long> cuisineFacets = new HashMap<>();
        Map<String, Long> priceRangeFacets = new HashMap<>();

        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            
            var cuisineAgg = aggregations.get("cuisine_types");
            if (cuisineAgg != null && cuisineAgg.aggregation().isSterms()) {
                StringTermsAggregate sterms = cuisineAgg.aggregation().sterms();
                for (StringTermsBucket bucket : sterms.buckets().array()) {
                    cuisineFacets.put(bucket.key().toString(), bucket.docCount());
                }
            }

            var priceAgg = aggregations.get("price_ranges");
            if (priceAgg != null && priceAgg.aggregation().isSterms()) {
                StringTermsAggregate sterms = priceAgg.aggregation().sterms();
                for (StringTermsBucket bucket : sterms.buckets().array()) {
                    priceRangeFacets.put(bucket.key().toString(), bucket.docCount());
                }
            }
        }

        return RestaurantSearchResult.builder()
                .restaurants(restaurants)
                .totalHits(searchHits.getTotalHits())
                .cuisineFacets(cuisineFacets)
                .priceRangeFacets(priceRangeFacets)
                .build();
    }

    @Override
    public List<String> autocomplete(String keyword) {
        log.info("Autocomplete search for keyword: {}", keyword);
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        NativeQuery query = new NativeQueryBuilder()
            .withQuery(q -> q.bool(b -> b
                .should(s -> s.prefix(p -> p.field("name").value(keyword.toLowerCase())))
                .should(s -> s.wildcard(w -> w.field("name").value("*" + keyword.toLowerCase() + "*")))
                .should(s -> s.prefix(p -> p.field("tags").value(keyword.toLowerCase())))
            ))
            .withPageable(PageRequest.of(0, 5))
            .build();

        SearchHits<RestaurantElasticDocument> searchHits = elasticsearchOperations.search(query, RestaurantElasticDocument.class);
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(RestaurantElasticDocument::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> suggestions(String keyword) {
        log.info("Spell correction / suggestions search for keyword: {}", keyword);
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        NativeQuery query = new NativeQueryBuilder()
            .withQuery(q -> q.match(m -> m
                .field("name")
                .query(keyword)
                .fuzziness("AUTO")
            ))
            .withPageable(PageRequest.of(0, 5))
            .build();

        SearchHits<RestaurantElasticDocument> searchHits = elasticsearchOperations.search(query, RestaurantElasticDocument.class);
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(RestaurantElasticDocument::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    private RestaurantElasticDocument toDocument(RestaurantSearchModel model) {
        if (model == null) return null;
        return RestaurantElasticDocument.builder()
                .id(model.getId() != null ? model.getId().toString() : null)
                .name(model.getName())
                .description(model.getDescription())
                .cuisineTypes(model.getCuisineTypes())
                .tags(model.getTags())
                .location(model.getLatitude() != null && model.getLongitude() != null ?
                        new GeoPoint(model.getLatitude(), model.getLongitude()) : null)
                .rating(model.getRating())
                .priceRange(model.getPriceRange())
                .avgDeliveryTime(model.getAvgDeliveryTime())
                .isOpen(model.getIsOpen())
                .isVeg(model.getIsVeg())
                .deliveryFee(model.getDeliveryFee())
                .totalOrders(model.getTotalOrders())
                .featured(model.getFeatured())
                .build();
    }

    private RestaurantSearchModel toModel(RestaurantElasticDocument doc) {
        if (doc == null) return null;
        return RestaurantSearchModel.builder()
                .id(doc.getId() != null ? UUID.fromString(doc.getId()) : null)
                .name(doc.getName())
                .description(doc.getDescription())
                .cuisineTypes(doc.getCuisineTypes())
                .tags(doc.getTags())
                .latitude(doc.getLocation() != null ? doc.getLocation().getLat() : null)
                .longitude(doc.getLocation() != null ? doc.getLocation().getLon() : null)
                .rating(doc.getRating())
                .priceRange(doc.getPriceRange())
                .avgDeliveryTime(doc.getAvgDeliveryTime())
                .isOpen(doc.getIsOpen())
                .isVeg(doc.getIsVeg())
                .deliveryFee(doc.getDeliveryFee())
                .totalOrders(doc.getTotalOrders())
                .featured(doc.getFeatured())
                .build();
    }
}
