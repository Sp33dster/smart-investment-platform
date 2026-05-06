package com.speedster.investment.smart_investment_platform.market.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketPriceResponse(
        String symbol,
        BigDecimal price,
        String currency,
        String source,
        Instant fetchedAt
) {}
