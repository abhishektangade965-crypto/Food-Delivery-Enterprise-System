package com.fooddelivery.restaurant.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.restaurant.domain.valueobject.MenuItemId;
import com.fooddelivery.restaurant.domain.valueobject.FoodType;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem extends BaseEntity<MenuItemId> {
    private String name;
    private String description;
    private Money basePrice;
    private Money discountedPrice;
    private FoodType foodType;
    private boolean isActive;
    private boolean isAvailable;
    private boolean isFeatured;
    private Integer calorieCount;
    private Integer prepTimeMinutes;
    private String imageUrl;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    @Builder.Default
    private List<String> allergens = new ArrayList<>();
    private Integer stockQuantity;
    private boolean trackInventory;
    private BigDecimal rating;
    @Builder.Default
    private List<MenuItemIngredient> ingredients = new ArrayList<>();

    public void updateStock(int quantity) {
        if (trackInventory) {
            this.stockQuantity = quantity;
            this.isAvailable = this.stockQuantity > 0;
        }
    }

    public void deductStock(int quantity) {
        if (trackInventory) {
            if (this.stockQuantity < quantity) {
                throw new IllegalStateException("Insufficient stock for item: " + name);
            }
            this.stockQuantity -= quantity;
            this.isAvailable = this.stockQuantity > 0;
        }
    }
}
