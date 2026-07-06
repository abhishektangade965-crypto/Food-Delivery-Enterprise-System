package com.fooddelivery.restaurant.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.restaurant.domain.valueobject.MenuCategoryId;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategory extends BaseEntity<MenuCategoryId> {
    private String name;
    private String description;
    private Integer displayOrder;
    private boolean isActive;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private String imageUrl;
    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();
}
