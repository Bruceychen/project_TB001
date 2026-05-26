package com.bruceychen.tb001.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "username is required")
        @Size(max = 100, message = "username must be at most 100 chars")
        String username
) {
}
