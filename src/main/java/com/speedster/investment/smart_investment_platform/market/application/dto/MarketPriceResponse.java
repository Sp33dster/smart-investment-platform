package com.speedster.investment.smart_investment_platform.market.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketPriceResponse(
        String symbol,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal price,
        String currency,
        String source,
        Instant fetchedAt
) {}
