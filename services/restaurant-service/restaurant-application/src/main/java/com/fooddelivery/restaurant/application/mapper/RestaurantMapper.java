package com.fooddelivery.restaurant.application.mapper;

import com.fooddelivery.common.domain.valueobject.Address;
import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.restaurant.application.dto.*;
import com.fooddelivery.restaurant.domain.entity.*;
import com.fooddelivery.restaurant.domain.valueobject.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalRatings", ignore = true)
    @Mapping(target = "surgeMultiplier", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "minOrderAmount", source = "minOrderAmount", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "deliveryFee", source = "deliveryFee", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "location", expression = "java(mapCoordinatesToLocation(request.latitude(), request.longitude()))")
    @Mapping(target = "address", expression = "java(mapRequestToAddress(request))")
    @Mapping(target = "slug", expression = "java(slugify(request.name()))")
    Restaurant createRequestToRestaurant(CreateRestaurantRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalRatings", ignore = true)
    @Mapping(target = "surgeMultiplier", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "minOrderAmount", source = "minOrderAmount", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "deliveryFee", source = "deliveryFee", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "location", expression = "java(mapCoordinatesToLocation(request.latitude(), request.longitude()))")
    @Mapping(target = "address", expression = "java(mapRequestToAddress(request))")
    @Mapping(target = "slug", expression = "java(slugify(request.name()))")
    void updateRestaurantFromRequest(UpdateRestaurantRequest request, @org.mapstruct.MappingTarget Restaurant restaurant);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapRestaurantIdToUuid")
    @Mapping(target = "minOrderAmount", source = "minOrderAmount", qualifiedByName = "mapMoneyToBigDecimal")
    @Mapping(target = "deliveryFee", source = "deliveryFee", qualifiedByName = "mapMoneyToBigDecimal")
    @Mapping(target = "latitude", expression = "java(restaurant.getLocation() != null ? restaurant.getLocation().latitude() : null)")
    @Mapping(target = "longitude", expression = "java(restaurant.getLocation() != null ? restaurant.getLocation().longitude() : null)")
    @Mapping(target = "street", expression = "java(restaurant.getAddress() != null ? restaurant.getAddress().street() : null)")
    @Mapping(target = "city", expression = "java(restaurant.getAddress() != null ? restaurant.getAddress().city() : null)")
    @Mapping(target = "state", expression = "java(restaurant.getAddress() != null ? restaurant.getAddress().state() : null)")
    @Mapping(target = "country", expression = "java(restaurant.getAddress() != null ? restaurant.getAddress().country() : null)")
    @Mapping(target = "postalCode", expression = "java(restaurant.getAddress() != null ? restaurant.getAddress().postalCode() : null)")
    @Mapping(target = "status", expression = "java(restaurant.getStatus() != null ? restaurant.getStatus().name() : null)")
    @Mapping(target = "approvalStatus", expression = "java(restaurant.getApprovalStatus() != null ? restaurant.getApprovalStatus().name() : null)")
    RestaurantResponse restaurantToRestaurantResponse(Restaurant restaurant);

    List<RestaurantResponse> restaurantsToRestaurantResponses(List<Restaurant> restaurants);

    // Categories
    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToMenuCategoryId")
    MenuCategory categoryDtoToCategory(MenuCategoryDto dto);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapMenuCategoryIdToUuid")
    MenuCategoryDto categoryToCategoryDto(MenuCategory category);

    List<MenuCategory> categoryDtosToCategories(List<MenuCategoryDto> dtos);
    List<MenuCategoryDto> categoriesToCategoryDtos(List<MenuCategory> categories);

    // Items
    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToMenuItemId")
    @Mapping(target = "basePrice", source = "basePrice", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "discountedPrice", source = "discountedPrice", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "foodType", source = "foodType", qualifiedByName = "mapStringToFoodType")
    MenuItem itemDtoToItem(MenuItemDto dto);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapMenuItemIdToUuid")
    @Mapping(target = "basePrice", source = "basePrice", qualifiedByName = "mapMoneyToBigDecimal")
    @Mapping(target = "discountedPrice", source = "discountedPrice", qualifiedByName = "mapMoneyToBigDecimal")
    @Mapping(target = "foodType", expression = "java(item.getFoodType() != null ? item.getFoodType().name() : null)")
    MenuItemDto itemToItemDto(MenuItem item);

    // Ingredients
    @Mapping(target = "inventoryItemId", source = "inventoryItemId", qualifiedByName = "mapUuidToInventoryItemId")
    MenuItemIngredient ingredientDtoToIngredient(MenuItemIngredientDto dto);

    @Mapping(target = "inventoryItemId", source = "inventoryItemId", qualifiedByName = "mapInventoryItemIdToUuid")
    MenuItemIngredientDto ingredientToIngredientDto(MenuItemIngredient ingredient);

    // Staff
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "role", qualifiedByName = "mapStringToStaffRole")
    RestaurantStaff staffDtoToStaff(RestaurantStaffDto dto);

    @Mapping(target = "role", expression = "java(staff.getRole() != null ? staff.getRole().name() : null)")
    RestaurantStaffDto staffToStaffDto(RestaurantStaff staff);

    // Inventory
    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToInventoryItemId")
    @Mapping(target = "costPerUnit", source = "costPerUnit", qualifiedByName = "mapBigDecimalToMoney")
    InventoryItem inventoryDtoToInventory(InventoryItemDto dto);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapInventoryItemIdToUuid")
    @Mapping(target = "costPerUnit", source = "costPerUnit", qualifiedByName = "mapMoneyToBigDecimal")
    InventoryItemDto inventoryToInventoryDto(InventoryItem inventory);

    // Custom named helper methods
    @Named("mapRestaurantIdToUuid")
    default UUID mapRestaurantIdToUuid(RestaurantId value) {
        return value == null ? null : value.getValue();
    }

    @Named("mapUuidToRestaurantId")
    default RestaurantId mapUuidToRestaurantId(UUID value) {
        return value == null ? null : new RestaurantId(value);
    }

    @Named("mapMenuCategoryIdToUuid")
    default UUID mapMenuCategoryIdToUuid(MenuCategoryId value) {
        return value == null ? null : value.getValue();
    }

    @Named("mapUuidToMenuCategoryId")
    default MenuCategoryId mapUuidToMenuCategoryId(UUID value) {
        return value == null ? null : new MenuCategoryId(value);
    }

    @Named("mapMenuItemIdToUuid")
    default UUID mapMenuItemIdToUuid(MenuItemId value) {
        return value == null ? null : value.getValue();
    }

    @Named("mapUuidToMenuItemId")
    default MenuItemId mapUuidToMenuItemId(UUID value) {
        return value == null ? null : new MenuItemId(value);
    }

    @Named("mapInventoryItemIdToUuid")
    default UUID mapInventoryItemIdToUuid(InventoryItemId value) {
        return value == null ? null : value.getValue();
    }

    @Named("mapUuidToInventoryItemId")
    default InventoryItemId mapUuidToInventoryItemId(UUID value) {
        return value == null ? null : new InventoryItemId(value);
    }

    @Named("mapBigDecimalToMoney")
    default Money mapBigDecimalToMoney(BigDecimal value) {
        return value == null ? null : new Money(value);
    }

    @Named("mapMoneyToBigDecimal")
    default BigDecimal mapMoneyToBigDecimal(Money value) {
        return value == null ? null : value.getAmount();
    }

    @Named("mapStringToFoodType")
    default FoodType mapStringToFoodType(String value) {
        return value == null ? null : FoodType.valueOf(value);
    }

    @Named("mapStringToStaffRole")
    default StaffRole mapStringToStaffRole(String value) {
        return value == null ? null : StaffRole.valueOf(value);
    }

    default GeoLocation mapCoordinatesToLocation(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return null;
        return GeoLocation.of(latitude, longitude);
    }

    default Address mapRequestToAddress(CreateRestaurantRequest request) {
        if (request.street() == null) return null;
        return new Address(
            request.street(),
            request.city(),
            request.state(),
            request.country(),
            request.postalCode(),
            request.latitude(),
            request.longitude()
        );
    }

    default Address mapRequestToAddress(UpdateRestaurantRequest request) {
        if (request.street() == null) return null;
        return new Address(
            request.street(),
            request.city(),
            request.state(),
            request.country(),
            request.postalCode(),
            request.latitude(),
            request.longitude()
        );
    }

    default String slugify(String name) {
        if (name == null) return null;
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-");
    }
}
