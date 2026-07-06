package com.fooddelivery.common.domain.exception;

/**
 * Exception thrown when a domain entity or aggregate cannot be found.
 * This maps to HTTP 404 Not Found at the API layer.
 */
public class DomainNotFoundException extends DomainException {

    private final String resourceType;
    private final Object resourceId;

    public DomainNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
        this.resourceType = null;
        this.resourceId = null;
    }

    public DomainNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s with id '%s' was not found", resourceType, resourceId), "RESOURCE_NOT_FOUND");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public DomainNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' was not found", resourceType, fieldName, fieldValue), "RESOURCE_NOT_FOUND");
        this.resourceType = resourceType;
        this.resourceId = fieldValue;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }
}
