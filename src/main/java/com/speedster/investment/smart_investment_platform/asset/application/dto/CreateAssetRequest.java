package com.speedster.investment.smart_investment_platform.asset.application.dto;

import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAssetRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Asset type is required")
        AssetType assetType,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
        BigDecimal quantity,

        @NotNull(message = "Purchase price is required")
        @DecimalMin(value = "0.01", message = "Purchase price must be greater than 0")
        BigDecimal purchasePrice,

        @NotBlank(message = "Currency is required")
        String currency,

        String notes
) {}
