package com.speedster.investment.smart_investment_platform.market.infrastructure.stooq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StooqClientTest {


    @InjectMocks
    private StooqClient stooqClient;

    @Test
    @DisplayName("Should support Polish stock symbols")
    void shouldSupportPolishStockSymbols() {
        assertThat(stooqClient.supports("CDR.PL")).isTrue();
        assertThat(stooqClient.supports("PKN.PL")).isTrue();
    }

    @Test
    @DisplayName("Should support US stock symbols")
    void shouldSupportUsStockSymbols() {
        assertThat(stooqClient.supports("AAPL.US")).isTrue();
        assertThat(stooqClient.supports("TSLA.US")).isTrue();
    }

    @Test
    @DisplayName("Should not support gold symbol")
    void shouldNotSupportGoldSymbol() {
        assertThat(stooqClient.supports("XAU")).isFalse();
    }

    @Test
    @DisplayName("Should not support null symbol")
    void shouldNotSupportNullSymbol() {
        assertThat(stooqClient.supports(null)).isFalse();
    }
}
