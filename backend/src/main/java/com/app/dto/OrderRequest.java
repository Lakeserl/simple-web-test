package com.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequest(
    @NotBlank @Size(max = 200) String customerName,
    @Size(max = 20) String customerPhone,
    @NotEmpty @Valid List<OrderItemRequest> items
) {}
