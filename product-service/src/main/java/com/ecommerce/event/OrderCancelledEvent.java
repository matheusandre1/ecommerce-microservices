package com.ecommerce.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCancelledEvent(
        long orderId,
        String customerName,
        BigDecimal totalAmount,
        List<OrderItem> items,
        LocalDateTime cancelledAt
) {
    public record OrderItem(String productId, int quantity) {}
}
