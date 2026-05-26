package com.bruceychen.tb001.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long orderId,
        Long userId,
        String username,
        Long productId,
        Long categoryId,
        String categoryName,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        Integer orderAmount,
        BigDecimal totalCost
) {
}
