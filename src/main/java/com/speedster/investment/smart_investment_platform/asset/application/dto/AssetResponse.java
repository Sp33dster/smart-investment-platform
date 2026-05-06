package com.speedster.investment.smart_investment_platform.asset.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String name,
        AssetType assetType,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal quantity,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal purchasePrice,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal currentValue,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal gainLoss,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal gainLossPercent,
        String currency,
        String notes,
        Instant createdAt
) {
}
