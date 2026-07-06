package com.fooddelivery.restaurant.domain.entity;

import com.fooddelivery.common.domain.entity.AggregateRoot;
import com.fooddelivery.common.domain.valueobject.Address;
import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.restaurant.domain.valueobject.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant extends AggregateRoot<RestaurantId> {
    private UUID ownerId;
    private String name;
    private String slug;
    private String description;
    @Builder.Default
    private List<String> cuisineTypes = new ArrayList<>();
    private RestaurantStatus status;
    private ApprovalStatus approvalStatus;
    private boolean isActive;
    private boolean isFeatured;
    private BigDecimal rating;
    private Integer totalRatings;
    private Money minOrderAmount;
    private Money deliveryFee;
    private Integer avgDeliveryTimeMinutes;
    private GeoLocation location;
    private Address address;
    private String bankDetails;
    private BigDecimal surgeMultiplier;
    private BigDecimal commissionRate;
    @Builder.Default
    private List<MenuCategory> categories = new ArrayList<>();
    @Builder.Default
    private List<RestaurantStaff> staff = new ArrayList<>();
    @Builder.Default
    private List<InventoryItem> inventory = new ArrayList<>();
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public void initializeRestaurant() {
        setId(RestaurantId.generate());
        this.status = RestaurantStatus.PENDING_APPROVAL;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.isActive = false;
        this.isFeatured = false;
        this.rating = BigDecimal.ZERO;
        this.totalRatings = 0;
        this.surgeMultiplier = BigDecimal.ONE;
        if (this.commissionRate == null) {
            this.commissionRate = new BigDecimal("0.10"); // Default 10% commission
        }
        if (this.cuisineTypes == null) {
            this.cuisineTypes = new ArrayList<>();
        }
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        if (this.staff == null) {
            this.staff = new ArrayList<>();
        }
        if (this.inventory == null) {
            this.inventory = new ArrayList<>();
        }
        ZonedDateTime now = ZonedDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void approve() {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.status = RestaurantStatus.ACTIVE;
        this.isActive = true;
        this.updatedAt = ZonedDateTime.now();
    }

    public void reject() {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.status = RestaurantStatus.INACTIVE;
        this.isActive = false;
        this.updatedAt = ZonedDateTime.now();
    }

    public void suspend() {
        this.status = RestaurantStatus.SUSPENDED;
        this.isActive = false;
        this.updatedAt = ZonedDateTime.now();
    }

    public void updateMenu(List<MenuCategory> newCategories) {
        this.categories = newCategories != null ? newCategories : new ArrayList<>();
        this.updatedAt = ZonedDateTime.now();
    }

    public void updateSurgeMultiplier(BigDecimal multiplier) {
        if (multiplier == null || multiplier.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Surge multiplier cannot be less than 1.0");
        }
        this.surgeMultiplier = multiplier;
        this.updatedAt = ZonedDateTime.now();
    }

    public void deductInventory(InventoryItemId itemId, BigDecimal quantity) {
        InventoryItem item = inventory.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + itemId.getValue()));
        item.deductStock(quantity);
        this.updatedAt = ZonedDateTime.now();
    }

    public void addInventory(InventoryItemId itemId, BigDecimal quantity) {
        InventoryItem item = inventory.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + itemId.getValue()));
        item.addStock(quantity);
        this.updatedAt = ZonedDateTime.now();
    }
}
