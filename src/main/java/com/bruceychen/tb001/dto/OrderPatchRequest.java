package com.bruceychen.tb001.dto;

import jakarta.validation.constraints.Min;

public record OrderPatchRequest(
        Long productId,

        @Min(value = 1, message = "orderAmount must be at least 1")
        Integer orderAmount
) {
}
