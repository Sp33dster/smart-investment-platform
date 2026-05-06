package com.speedster.investment.smart_investment_platform.market.infrastructure.goldapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoldApiResponse(
        @JsonProperty("price")BigDecimal price,
        @JsonProperty("currency") String currency,
        @JsonProperty("timestamp") Long timestamp
        ) {}
