package com.speedster.investment.smart_investment_platform.asset.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PortfolioSummaryResponse(
        List<AssetResponse> assets,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal totalPurchaseValue,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal totalCurrentValue,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal totalGainLoss,
        BigDecimal totalGainLossPercent,
        Map<AssetType, BigDecimal> valueByType
) {
}
