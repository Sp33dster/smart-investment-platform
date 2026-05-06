package com.speedster.investment.smart_investment_platform.market.domain;

import java.util.Optional;

public interface PriceProvider {
    Optional<MarketPrice> fetchCurrentPrice(String symbol);
    boolean supports(String symbol);
}
