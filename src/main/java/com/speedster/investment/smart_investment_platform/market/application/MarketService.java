package com.speedster.investment.smart_investment_platform.market.application;

import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPriceRepository;
import com.speedster.investment.smart_investment_platform.market.domain.PriceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

    private final List<PriceProvider> priceProviders;
    private final MarketPriceRepository marketPriceRepository;

    public Optional<MarketPrice> getCurrentPrice(String symbol){
        return  priceProviders.stream()
                .filter(provider -> provider.supports(symbol))
                .findFirst()
                .flatMap(provider -> provider.fetchCurrentPrice(symbol));
    }

    public Optional<MarketPrice> getLatestStoredPrice(String symbol){
        return marketPriceRepository.findLatestBySymbol(symbol);
    }
}
