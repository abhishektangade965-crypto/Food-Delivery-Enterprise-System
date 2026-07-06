package com.fooddelivery.search.dataaccess.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "restaurants")
public class RestaurantElasticDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private List<String> cuisineTypes;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Keyword)
    private String priceRange;

    @Field(type = FieldType.Integer)
    private Integer avgDeliveryTime;

    @Field(type = FieldType.Boolean)
    private Boolean isOpen;

    @Field(type = FieldType.Boolean)
    private Boolean isVeg;

    @Field(type = FieldType.Double)
    private BigDecimal deliveryFee;

    @Field(type = FieldType.Integer)
    private Integer totalOrders;

    @Field(type = FieldType.Boolean)
    private Boolean featured;
}
