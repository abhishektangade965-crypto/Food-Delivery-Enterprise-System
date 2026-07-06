package com.fooddelivery.restaurant.dataaccess.entity;

import com.fooddelivery.restaurant.domain.valueobject.FoodType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "menu_items")
@Entity
public class MenuItemJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategoryJpaEntity category;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal basePrice;

    private BigDecimal discountedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodType foodType;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isAvailable;

    @Column(nullable = false)
    private boolean isFeatured;

    private Integer calorieCount;
    private Integer prepTimeMinutes;
    private String imageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "menu_item_tags", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "tag")
    private List<String> tags;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "menu_item_allergens", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "allergen")
    private List<String> allergens;

    private Integer stockQuantity;

    @Column(nullable = false)
    private boolean trackInventory;

    private BigDecimal rating;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "menu_item_ingredients", joinColumns = @JoinColumn(name = "menu_item_id"))
    private List<MenuItemIngredientEmbeddable> ingredients;
}
