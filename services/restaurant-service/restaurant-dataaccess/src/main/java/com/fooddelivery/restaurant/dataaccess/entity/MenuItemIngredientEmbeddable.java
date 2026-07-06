package com.fooddelivery.restaurant.dataaccess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MenuItemIngredientEmbeddable {

    @Column(nullable = false)
    private UUID inventoryItemId;

    @Column(nullable = false)
    private BigDecimal quantityNeeded;

    @Column(nullable = false)
    private String unit;
}
