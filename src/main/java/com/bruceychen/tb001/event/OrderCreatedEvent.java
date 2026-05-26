package com.bruceychen.tb001.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        String username,
        Long productId,
        Integer orderAmount,
        BigDecimal totalCost
) {
}
