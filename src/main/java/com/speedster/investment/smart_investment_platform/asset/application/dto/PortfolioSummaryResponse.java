package com.speedster.investment.smart_investment_platform.asset.application.dto;

import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PortfolioSummaryResponse(
        List<AssetResponse> assets,
        BigDecimal totalPurchaseValue,
        BigDecimal totalCurrentValue,
        BigDecimal totalGainLoss,
        BigDecimal totalGainLossPercent,
        Map<AssetType, BigDecimal> valueByType
) {
}
