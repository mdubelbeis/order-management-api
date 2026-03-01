package com.masondubelbeis.order_management_api.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String name,
        Instant createdAt
) {
}
