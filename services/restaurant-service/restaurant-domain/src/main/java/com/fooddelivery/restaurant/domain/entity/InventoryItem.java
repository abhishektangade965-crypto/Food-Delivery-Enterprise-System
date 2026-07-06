package com.fooddelivery.restaurant.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.restaurant.domain.valueobject.InventoryItemId;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem extends BaseEntity<InventoryItemId> {
    private String name;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal reorderLevel;
    private BigDecimal maxStock;
    private Money costPerUnit;
    private String supplierInfo;

    public void deductStock(BigDecimal quantity) {
        if (this.currentStock.compareTo(quantity) < 0) {
            throw new IllegalStateException("Insufficient inventory stock for item: " + name);
        }
        this.currentStock = this.currentStock.subtract(quantity);
    }

    public void addStock(BigDecimal quantity) {
        BigDecimal newStock = this.currentStock.add(quantity);
        if (this.maxStock != null && newStock.compareTo(this.maxStock) > 0) {
            this.currentStock = this.maxStock;
        } else {
            this.currentStock = newStock;
        }
    }

    public boolean isBelowReorderLevel() {
        return this.reorderLevel != null && this.currentStock.compareTo(this.reorderLevel) < 0;
    }
}
