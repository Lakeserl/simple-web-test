package com.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 1000) String description,
    @DecimalMin("0.0") BigDecimal price,
    @Size(max = 100) String category,
    @Min(0) Integer stock,
    @Size(max = 500) String imageUrl
) {}
