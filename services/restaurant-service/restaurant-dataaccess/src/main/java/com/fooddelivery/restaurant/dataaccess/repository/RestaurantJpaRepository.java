package com.fooddelivery.restaurant.dataaccess.repository;

import com.fooddelivery.restaurant.dataaccess.entity.RestaurantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity, UUID> {
    Optional<RestaurantJpaEntity> findBySlug(String slug);
    List<RestaurantJpaEntity> findByOwnerId(UUID ownerId);

    @Query(value = "SELECT * FROM restaurants r WHERE r.status = 'ACTIVE' AND r.is_active = true AND " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(r.latitude)))) <= :radiusKm",
            nativeQuery = true)
    List<RestaurantJpaEntity> findActiveNear(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm);
}
