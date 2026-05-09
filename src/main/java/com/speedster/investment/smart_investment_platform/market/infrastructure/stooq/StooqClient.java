package com.speedster.investment.smart_investment_platform.market.infrastructure.stooq;

import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPriceRepository;
import com.speedster.investment.smart_investment_platform.market.domain.PriceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StooqClient implements PriceProvider {

    private final RestClient restClient;
    private final MarketPriceRepository marketPriceRepository;

    private static final String STOOQ_URL = "https://stooq.com/q/l/?s={symbol}&f=sd2t2ohlcv&h&e=csv";
    private static final int CLOSE_PRICE_INDEX = 6;
    private static final int MIN_COLUMNS = 7;

    @Override
    public boolean supports(String symbol){
        return symbol != null && (symbol.endsWith(".PL") || symbol.endsWith(".US"));
    }

    @Override
    public Optional<MarketPrice> fetchCurrentPrice(String symbol){
        try {
            String stooqSymbol = normalizeSymbol(symbol);

            String csvResponse = restClient.get()
                    .uri(STOOQ_URL,stooqSymbol)
                    .retrieve()
                    .body(String.class);

            return parseCsvAndSave(csvResponse, symbol);
        } catch (Exception e) {
            log.error("Failed to fetch stock price for {}: {}",
                    symbol, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<MarketPrice> parseCsvAndSave(String csv, String symbol){
        return extractLines(csv, symbol)
                .flatMap(lines -> extractPrice(lines, symbol))
                .map(price -> buildMarketPrice(symbol, price))
                .map(marketPriceRepository::save);
    }

    private String normalizeSymbol(String symbol) {
        return symbol.contains(".")
                ? symbol.substring(0, symbol.lastIndexOf('.'))
                : symbol;
    }

    private Optional<String[]> extractLines(String csv, String symbol) {
        if (csv == null || csv.isBlank()) {
            log.warn("Empty response from Stooq for {}", symbol);
            return Optional.empty();
        }

        String[] lines = csv.trim().split("\n");
        if (lines.length < 2) {
            log.warn("Invalid CSV format from Stooq for {}", symbol);
            return Optional.empty();
        }

        return Optional.of(lines);
    }

    private Optional<BigDecimal> extractPrice(String[] lines, String symbol) {
        String[] values = lines[1].split(",");

        if (values.length < MIN_COLUMNS) {
            log.warn("Insufficient CSV columns from Stooq for {}", symbol);
            return Optional.empty();
        }

        try {
            BigDecimal price = new BigDecimal(values[CLOSE_PRICE_INDEX].trim());
            return validatePrice(price, symbol);

        } catch (NumberFormatException e) {
            log.error("Failed to parse price from Stooq for {}: {}",
                    symbol, lines[1]);
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> validatePrice(BigDecimal price, String symbol) {
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Stooq returned zero price for {} — market may be closed",
                    symbol);
            return Optional.empty();
        }

        log.info("Fetched stock price: {} = {} PLN", symbol, price);
        return Optional.of(price);
    }

    private MarketPrice buildMarketPrice(String symbol, BigDecimal price) {
        return MarketPrice.builder()
                .symbol(symbol)
                .price(price)
                .currency("PLN")
                .source("STOOQ")
                .fetchedAt(Instant.now())
                .build();
    }
}
