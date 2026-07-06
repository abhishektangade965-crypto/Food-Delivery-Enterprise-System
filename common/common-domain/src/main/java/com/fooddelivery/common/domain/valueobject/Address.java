package com.fooddelivery.common.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * Immutable value object representing a physical address with optional geographic coordinates.
 */
public record Address(
        @JsonProperty("street") String street,
        @JsonProperty("city") String city,
        @JsonProperty("state") String state,
        @JsonProperty("country") String country,
        @JsonProperty("postalCode") String postalCode,
        @JsonProperty("latitude") Double latitude,
        @JsonProperty("longitude") Double longitude
) {

    @JsonCreator
    public Address {
        if (StringUtils.isBlank(street)) {
            throw new IllegalArgumentException("Street cannot be blank");
        }
        if (StringUtils.isBlank(city)) {
            throw new IllegalArgumentException("City cannot be blank");
        }
        if (StringUtils.isBlank(country)) {
            throw new IllegalArgumentException("Country cannot be blank");
        }
        if (StringUtils.isBlank(postalCode)) {
            throw new IllegalArgumentException("Postal code cannot be blank");
        }
        if (latitude != null && (latitude < -90.0 || latitude > 90.0)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude != null && (longitude < -180.0 || longitude > 180.0)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
        street = street.trim();
        city = city.trim();
        state = state != null ? state.trim() : null;
        country = country.trim().toUpperCase();
        postalCode = postalCode.trim();
    }

    public static Address of(String street, String city, String state, String country, String postalCode) {
        return new Address(street, city, state, country, postalCode, null, null);
    }

    public static Address of(String street, String city, String state, String country,
                             String postalCode, double latitude, double longitude) {
        return new Address(street, city, state, country, postalCode, latitude, longitude);
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public GeoLocation toGeoLocation() {
        if (!hasCoordinates()) {
            throw new IllegalStateException("Address does not have geographic coordinates");
        }
        return GeoLocation.of(latitude, longitude);
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        if (StringUtils.isNotBlank(city)) sb.append(", ").append(city);
        if (StringUtils.isNotBlank(state)) sb.append(", ").append(state);
        sb.append(", ").append(country);
        if (StringUtils.isNotBlank(postalCode)) sb.append(" ").append(postalCode);
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}
