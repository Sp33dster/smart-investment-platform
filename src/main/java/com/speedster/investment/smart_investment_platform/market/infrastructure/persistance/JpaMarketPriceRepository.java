package com.speedster.investment.smart_investment_platform.market.infrastructure.persistance;

import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPriceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaMarketPriceRepository extends JpaRepository<MarketPrice, UUID>, MarketPriceRepository {

    Optional<MarketPrice> findTopBySymbolOrderByFetchedAtDesc(String symbol);
    List<MarketPrice> findBySymbolOrderByFetchedAtDesc(String symbol);

    default Optional<MarketPrice> findLatestBySymbol(String symbol){
        return findTopBySymbolOrderByFetchedAtDesc(symbol);
    }
}
