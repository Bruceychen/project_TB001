package com.bruceychen.tb001.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "orderAmount is required")
        @Min(value = 1, message = "orderAmount must be at least 1")
        Integer orderAmount
) {
}
