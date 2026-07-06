package com.fooddelivery.restaurant.domain.entity;

import com.fooddelivery.restaurant.domain.valueobject.InventoryItemId;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemIngredient {
    private InventoryItemId inventoryItemId;
    private BigDecimal quantityNeeded;
    private String unit;
}
