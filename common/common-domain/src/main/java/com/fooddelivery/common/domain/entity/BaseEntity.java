package com.fooddelivery.common.domain.entity;

import java.util.Objects;

/**
 * Abstract base class for all domain entities.
 * Equality is based on the entity's identity (ID), not its attributes.
 * @param <ID> the type of the entity's identifier
 */
public abstract class BaseEntity<ID> {

    private ID id;

    protected BaseEntity() {
    }

    protected BaseEntity(ID id) {
        this.id = id;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(id=" + id + ")";
    }
}
