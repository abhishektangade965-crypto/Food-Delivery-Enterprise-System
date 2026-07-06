package com.fooddelivery.delivery.dataaccess.mapper;

import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.delivery.dataaccess.entity.DeliveryAssignmentEntity;
import com.fooddelivery.delivery.dataaccess.entity.DriverEntity;
import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;
import com.fooddelivery.delivery.domain.entity.Driver;
import com.fooddelivery.delivery.domain.valueobject.AssignmentId;
import com.fooddelivery.delivery.domain.valueobject.DriverId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeliveryDataAccessMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "driverIdToUuid")
    @Mapping(target = "latitude", source = "location.latitude")
    @Mapping(target = "longitude", source = "location.longitude")
    DriverEntity driverToDriverEntity(Driver driver);

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToDriverId")
    @Mapping(target = "location", source = "driverEntity", qualifiedByName = "driverEntityToGeoLocation")
    Driver driverEntityToDriver(DriverEntity driverEntity);

    @Mapping(target = "id", source = "id", qualifiedByName = "assignmentIdToUuid")
    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "orderIdToUuid")
    @Mapping(target = "driverId", source = "driverId", qualifiedByName = "driverIdToUuid")
    @Mapping(target = "pickupLatitude", source = "pickupLocation.latitude")
    @Mapping(target = "pickupLongitude", source = "pickupLocation.longitude")
    @Mapping(target = "dropoffLatitude", source = "dropoffLocation.latitude")
    @Mapping(target = "dropoffLongitude", source = "dropoffLocation.longitude")
    DeliveryAssignmentEntity deliveryAssignmentToDeliveryAssignmentEntity(DeliveryAssignment assignment);

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToAssignmentId")
    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "uuidToOrderId")
    @Mapping(target = "driverId", source = "driverId", qualifiedByName = "uuidToDriverId")
    @Mapping(target = "pickupLocation", source = "entity", qualifiedByName = "entityToPickupLocation")
    @Mapping(target = "dropoffLocation", source = "entity", qualifiedByName = "entityToDropoffLocation")
    DeliveryAssignment deliveryAssignmentEntityToDeliveryAssignment(DeliveryAssignmentEntity entity);

    @Named("driverIdToUuid")
    default UUID driverIdToUuid(DriverId driverId) {
        return driverId != null ? driverId.getValue() : null;
    }

    @Named("uuidToDriverId")
    default DriverId uuidToDriverId(UUID uuid) {
        return uuid != null ? new DriverId(uuid) : null;
    }

    @Named("assignmentIdToUuid")
    default UUID assignmentIdToUuid(AssignmentId assignmentId) {
        return assignmentId != null ? assignmentId.getValue() : null;
    }

    @Named("uuidToAssignmentId")
    default AssignmentId uuidToAssignmentId(UUID uuid) {
        return uuid != null ? new AssignmentId(uuid) : null;
    }

    @Named("orderIdToUuid")
    default UUID orderIdToUuid(OrderId orderId) {
        return orderId != null ? orderId.getValue() : null;
    }

    @Named("uuidToOrderId")
    default OrderId uuidToOrderId(UUID uuid) {
        return uuid != null ? new OrderId(uuid) : null;
    }

    @Named("driverEntityToGeoLocation")
    default GeoLocation driverEntityToGeoLocation(DriverEntity driverEntity) {
        if (driverEntity == null || driverEntity.getLatitude() == null || driverEntity.getLongitude() == null) {
            return null;
        }
        return GeoLocation.of(driverEntity.getLatitude(), driverEntity.getLongitude());
    }

    @Named("entityToPickupLocation")
    default GeoLocation entityToPickupLocation(DeliveryAssignmentEntity entity) {
        if (entity == null || entity.getPickupLatitude() == null || entity.getPickupLongitude() == null) {
            return null;
        }
        return GeoLocation.of(entity.getPickupLatitude(), entity.getPickupLongitude());
    }

    @Named("entityToDropoffLocation")
    default GeoLocation entityToDropoffLocation(DeliveryAssignmentEntity entity) {
        if (entity == null || entity.getDropoffLatitude() == null || entity.getDropoffLongitude() == null) {
            return null;
        }
        return GeoLocation.of(entity.getDropoffLatitude(), entity.getDropoffLongitude());
    }
}
