package com.speedster.investment.smart_investment_platform.market.application;

import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPriceRepository;
import com.speedster.investment.smart_investment_platform.market.domain.PriceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock private MarketPriceRepository marketPriceRepository;

    @Mock private PriceProvider goldProvider;
    @Mock private PriceProvider stooqProvider;

    private MarketService marketService;

    private MarketPrice goldPrice;

    @BeforeEach
    void setUp() {

        marketService = new MarketService(
                List.of(goldProvider, stooqProvider),
                marketPriceRepository
        );

        goldPrice = MarketPrice.builder()
                .symbol("XAU")
                .price(new BigDecimal("16000.00"))
                .currency("PLN")
                .source("GOLD_API")
                .fetchedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should use correct provider for gold symbol")
    void shouldUseCorrectProviderForGoldSymbol() {
        // given
        given(goldProvider.supports("XAU")).willReturn(true);
        given(goldProvider.fetchCurrentPrice("XAU"))
                .willReturn(Optional.of(goldPrice));

        // when
        Optional<MarketPrice> result = marketService.getCurrentPrice("XAU");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("XAU");
        then(goldProvider).should().fetchCurrentPrice("XAU");
        then(stooqProvider).should(never()).fetchCurrentPrice(any());
    }

    @Test
    @DisplayName("Should use correct provider for stock symbol")
    void shouldUseCorrectProviderForStockSymbol() {
        // given
        MarketPrice stockPrice = MarketPrice.builder()
                .symbol("CDR.PL")
                .price(new BigDecimal("260.00"))
                .currency("PLN")
                .source("STOOQ")
                .fetchedAt(Instant.now())
                .build();

        given(goldProvider.supports("CDR.PL")).willReturn(false);
        given(stooqProvider.supports("CDR.PL")).willReturn(true);
        given(stooqProvider.fetchCurrentPrice("CDR.PL"))
                .willReturn(Optional.of(stockPrice));

        // when
        Optional<MarketPrice> result = marketService.getCurrentPrice("CDR.PL");

        // then
        assertThat(result).isPresent();
        then(stooqProvider).should().fetchCurrentPrice("CDR.PL");
        then(goldProvider).should(never()).fetchCurrentPrice(any());
    }

    @Test
    @DisplayName("Should return empty when no provider supports symbol")
    void shouldReturnEmptyWhenNoProviderSupportsSymbol() {
        // given
        given(goldProvider.supports("UNKNOWN")).willReturn(false);
        given(stooqProvider.supports("UNKNOWN")).willReturn(false);

        // when
        Optional<MarketPrice> result = marketService.getCurrentPrice("UNKNOWN");

        // then
        assertThat(result).isEmpty();
        then(goldProvider).should(never()).fetchCurrentPrice(any());
        then(stooqProvider).should(never()).fetchCurrentPrice(any());
    }

    @Test
    @DisplayName("Should return latest stored price from repository")
    void shouldReturnLatestStoredPriceFromRepository() {
        // given
        given(marketPriceRepository.findLatestBySymbol("XAU"))
                .willReturn(Optional.of(goldPrice));

        // when
        Optional<MarketPrice> result =
                marketService.getLatestStoredPrice("XAU");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPrice())
                .isEqualByComparingTo(new BigDecimal("16000.00"));
    }

    @Test
    @DisplayName("Should return empty when no stored price found")
    void shouldReturnEmptyWhenNoStoredPriceFound() {
        // given
        given(marketPriceRepository.findLatestBySymbol("XAU"))
                .willReturn(Optional.empty());

        // when
        Optional<MarketPrice> result =
                marketService.getLatestStoredPrice("XAU");

        // then
        assertThat(result).isEmpty();
    }
}
