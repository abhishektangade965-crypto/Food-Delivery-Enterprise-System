package com.fooddelivery.restaurant.application.dto;

import java.util.List;

public record UpdateMenuRequest(
    List<MenuCategoryDto> categories
) {}
