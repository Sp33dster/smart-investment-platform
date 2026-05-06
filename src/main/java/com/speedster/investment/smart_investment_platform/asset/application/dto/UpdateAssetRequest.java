package com.speedster.investment.smart_investment_platform.asset.application.dto;

import java.math.BigDecimal;

public record UpdateAssetRequest(
        String name,
        BigDecimal quantity,
        BigDecimal purchasePrice,
        String notes
) {
}
