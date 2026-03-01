package com.masondubelbeis.order_management_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull @DecimalMin("0.00")BigDecimal price,
        @NotNull @Min(0) Integer inventoryQty
        ) {
}
