package com.speedster.investment.smart_investment_platform.market.infrastructure.stooq;

import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPriceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StooqClientParsingTest {

    @Mock
    private RestClient restClient;
    @Mock private MarketPriceRepository marketPriceRepository;

    @InjectMocks
    private StooqClient stooqClient;

    @Test
    @DisplayName("Should parse valid CSV and save market price")
    void shouldParseValidCsvAndSave() {
        // given
        String validCsv = "Symbol,Date,Time,Open,High,Low,Close,Volume\n" +
                "CDR,2026-05-09,17:00:00,260.00,265.00,258.00,262.50,1500000";

        MarketPrice savedPrice = MarketPrice.builder()
                .symbol("CDR.PL")
                .price(new BigDecimal("262.50"))
                .currency("PLN")
                .source("STOOQ")
                .fetchedAt(Instant.now())
                .build();

        given(marketPriceRepository.save(any(MarketPrice.class)))
                .willReturn(savedPrice);

        // when
        RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        given(restClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(anyString(), anyString())).willReturn(uriSpec);
        given(uriSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(String.class)).willReturn(validCsv);

        // when
        Optional<MarketPrice> result = stooqClient.fetchCurrentPrice("CDR.PL");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPrice())
                .isEqualByComparingTo(new BigDecimal("262.50"));
        then(marketPriceRepository).should().save(any(MarketPrice.class));
    }

    @Test
    @DisplayName("Should return empty when Stooq returns N/D")
    void shouldReturnEmptyWhenStooqReturnsNd() {
        // given
        String ndCsv = "Symbol,Date,Time,Open,High,Low,Close,Volume\n" +
                "CDR,N/D,N/D,N/D,N/D,N/D,N/D,N/D";

        RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        given(restClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(anyString(), anyString())).willReturn(uriSpec);
        given(uriSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(String.class)).willReturn(ndCsv);

        // when
        Optional<MarketPrice> result = stooqClient.fetchCurrentPrice("CDR.PL");

        // then
        assertThat(result).isEmpty();
        then(marketPriceRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should return empty when CSV is empty")
    void shouldReturnEmptyWhenCsvIsEmpty() {
        // given
        RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        given(restClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(anyString(), anyString())).willReturn(uriSpec);
        given(uriSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(String.class)).willReturn("");

        // when
        Optional<MarketPrice> result = stooqClient.fetchCurrentPrice("CDR.PL");

        // then
        assertThat(result).isEmpty();
        then(marketPriceRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should return empty when price is zero")
    void shouldReturnEmptyWhenPriceIsZero() {
        // given
        String zeroCsv = "Symbol,Date,Time,Open,High,Low,Close,Volume\n" +
                "CDR,2026-05-09,17:00:00,0,0,0,0,0";

        RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        given(restClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(anyString(), anyString())).willReturn(uriSpec);
        given(uriSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(String.class)).willReturn(zeroCsv);

        // when
        Optional<MarketPrice> result = stooqClient.fetchCurrentPrice("CDR.PL");

        // then
        assertThat(result).isEmpty();
        then(marketPriceRepository).should(never()).save(any());
    }
}
