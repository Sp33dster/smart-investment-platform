package com.speedster.investment.smart_investment_platform.market.infrastructure.goldapi;

import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPriceRepository;
import com.speedster.investment.smart_investment_platform.market.domain.PriceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoldApiClient implements PriceProvider {

    private final RestClient restClient;
    private final MarketPriceRepository marketPriceRepository;

    @Value("${market.gold-api.url}")
    private String apiUrl;

    @Value("${market.gold-api.api-key}")
    private String apiKey;

    @Value("${market.gold-api.symbol}")
    private String symbol;

    @Value("${market.gold-api.currency}")
    private String currency;

    @Override
    public boolean supports(String symbol){
        return "XAU".equalsIgnoreCase(symbol)
                || "GOLD".equalsIgnoreCase(symbol);
    }

    @Override
    public Optional<MarketPrice> fetchCurrentPrice(String symbol) {

        try {
            GoldApiResponse response = restClient.get()
                    .uri(apiUrl + "/" + symbol + "/" + currency)
                    .header("x-access-token", apiKey)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .body(GoldApiResponse.class);

            if (response == null || response.price() == null) {
                log.warn("Gold API returned empty response");
                return Optional.empty();
            }

            MarketPrice marketPrice = MarketPrice.builder()
                    .symbol("XAU")
                    .price(response.price())
                    .currency(response.currency())
                    .source("GOLD_API")
                    .fetchedAt(Instant.now())
                    .build();

            MarketPrice saved = marketPriceRepository.save(marketPrice);
            log.info("Fetches gold price: {} {}", saved.getPrice(), saved.getCurrency());

            return Optional.of(saved);
        } catch (Exception e) {
            log.error("Failed to fetch gold price: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
