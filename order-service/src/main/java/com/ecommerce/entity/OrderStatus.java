package com.ecommerce.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}