package com.speedster.investment.smart_investment_platform.market.domain;

import java.util.List;
import java.util.Optional;

public interface MarketPriceRepository {
    MarketPrice save(MarketPrice price);
    Optional<MarketPrice> findLatestBySymbol(String symbol);
    List<MarketPrice> findBySymbolOrderByFetchedAtDesc(String symbol);
}
