package com.fooddelivery.common.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable geographic location represented as latitude/longitude.
 * Provides distance calculation using the Haversine formula.
 */
public record GeoLocation(
        @JsonProperty("latitude") double latitude,
        @JsonProperty("longitude") double longitude
) {
    private static final double EARTH_RADIUS_KM = 6371.0;

    @JsonCreator
    public GeoLocation {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees, got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees, got: " + longitude);
        }
    }

    public static GeoLocation of(double latitude, double longitude) {
        return new GeoLocation(latitude, longitude);
    }

    /**
     * Calculates the great-circle distance between this location and another
     * using the Haversine formula.
     *
     * @param other the other geographic location
     * @return the distance in kilometers
     */
    public double distanceInKilometers(GeoLocation other) {
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates the distance in miles between this location and another.
     */
    public double distanceInMiles(GeoLocation other) {
        return distanceInKilometers(other) * 0.621371;
    }

    /**
     * Returns true if the other location is within the specified radius in kilometers.
     */
    public boolean isWithinRadiusKm(GeoLocation other, double radiusKm) {
        return distanceInKilometers(other) <= radiusKm;
    }

    /**
     * Returns the bearing in degrees from this location to the other.
     */
    public double bearingTo(GeoLocation other) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    @Override
    public String toString() {
        return String.format("GeoLocation{lat=%.6f, lon=%.6f}", latitude, longitude);
    }
}
