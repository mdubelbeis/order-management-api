package com.masondubelbeis.order_management_api.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        Integer inventoryQty
) {
}
