package com.fooddelivery.restaurant.dataaccess.mapper;

import com.fooddelivery.common.domain.valueobject.Address;
import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.restaurant.dataaccess.entity.*;
import com.fooddelivery.restaurant.domain.entity.*;
import com.fooddelivery.restaurant.domain.valueobject.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RestaurantDataAccessMapper {

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
    RestaurantJpaEntity restaurantToRestaurantJpaEntity(Restaurant restaurant);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToRestaurantId")
    @Mapping(target = "minOrderAmount", source = "minOrderAmount", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "deliveryFee", source = "deliveryFee", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "location", expression = "java(mapCoordinatesToLocation(entity.getLatitude(), entity.getLongitude()))")
    @Mapping(target = "address", expression = "java(mapEntityToAddress(entity))")
    Restaurant restaurantJpaEntityToRestaurant(RestaurantJpaEntity entity);

    List<Restaurant> restaurantJpaEntitiesToRestaurants(List<RestaurantJpaEntity> entities);

    // Menu Categories
    @Mapping(target = "id", source = "id", qualifiedByName = "mapMenuCategoryIdToUuid")
    @Mapping(target = "restaurant", ignore = true)
    MenuCategoryJpaEntity categoryToCategoryJpaEntity(MenuCategory category);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToMenuCategoryId")
    MenuCategory categoryJpaEntityToCategory(MenuCategoryJpaEntity entity);

    // Menu Items
    @Mapping(target = "id", source = "id", qualifiedByName = "mapMenuItemIdToUuid")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "basePrice", source = "basePrice", qualifiedByName = "mapMoneyToBigDecimal")
    @Mapping(target = "discountedPrice", source = "discountedPrice", qualifiedByName = "mapMoneyToBigDecimal")
    MenuItemJpaEntity itemToItemJpaEntity(MenuItem item);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToMenuItemId")
    @Mapping(target = "basePrice", source = "basePrice", qualifiedByName = "mapBigDecimalToMoney")
    @Mapping(target = "discountedPrice", source = "discountedPrice", qualifiedByName = "mapBigDecimalToMoney")
    MenuItem itemJpaEntityToMenuItem(MenuItemJpaEntity entity);

    // Ingredients
    @Mapping(target = "inventoryItemId", source = "inventoryItemId", qualifiedByName = "mapInventoryItemIdToUuid")
    MenuItemIngredientEmbeddable ingredientToIngredientEmbeddable(MenuItemIngredient ingredient);

    @Mapping(target = "inventoryItemId", source = "inventoryItemId", qualifiedByName = "mapUuidToInventoryItemId")
    MenuItemIngredient ingredientEmbeddableToIngredient(MenuItemIngredientEmbeddable entity);

    // Staff
    @Mapping(target = "id", source = "id")
    @Mapping(target = "restaurant", ignore = true)
    RestaurantStaffJpaEntity staffToStaffJpaEntity(RestaurantStaff staff);

    @Mapping(target = "id", source = "id")
    RestaurantStaff staffJpaEntityToStaff(RestaurantStaffJpaEntity entity);

    // Inventory
    @Mapping(target = "id", source = "id", qualifiedByName = "mapInventoryItemIdToUuid")
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "costPerUnit", source = "costPerUnit", qualifiedByName = "mapMoneyToBigDecimal")
    InventoryItemJpaEntity inventoryToInventoryJpaEntity(InventoryItem inventory);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToInventoryItemId")
    @Mapping(target = "costPerUnit", source = "costPerUnit", qualifiedByName = "mapBigDecimalToMoney")
    InventoryItem inventoryJpaEntityToInventory(InventoryItemJpaEntity entity);

    // Named Conversions
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

    default GeoLocation mapCoordinatesToLocation(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return null;
        return GeoLocation.of(latitude, longitude);
    }

    default Address mapEntityToAddress(RestaurantJpaEntity entity) {
        if (entity.getStreet() == null) return null;
        return new Address(
                entity.getStreet(),
                entity.getCity(),
                entity.getState(),
                entity.getCountry(),
                entity.getPostalCode(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }

    @AfterMapping
    default void establishRelationships(Restaurant restaurant, @MappingTarget RestaurantJpaEntity restaurantJpaEntity) {
        if (restaurantJpaEntity.getCategories() != null) {
            restaurantJpaEntity.getCategories().forEach(category -> {
                category.setRestaurant(restaurantJpaEntity);
                if (category.getItems() != null) {
                    category.getItems().forEach(item -> item.setCategory(category));
                }
            });
        }
        if (restaurantJpaEntity.getStaff() != null) {
            restaurantJpaEntity.getStaff().forEach(staff -> staff.setRestaurant(restaurantJpaEntity));
        }
        if (restaurantJpaEntity.getInventory() != null) {
            restaurantJpaEntity.getInventory().forEach(inv -> inv.setRestaurant(restaurantJpaEntity));
        }
    }
}
