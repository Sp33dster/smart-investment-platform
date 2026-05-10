package com.speedster.investment.smart_investment_platform.asset.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateAssetValueRequest(

        @NotNull
        @DecimalMin(value = "0.01", message = "Value must be greate than 0")
        BigDecimal currentValue
) {
}
