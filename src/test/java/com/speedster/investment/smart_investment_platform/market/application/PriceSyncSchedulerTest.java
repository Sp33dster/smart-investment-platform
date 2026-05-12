package com.speedster.investment.smart_investment_platform.market.application;

import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.shared.event.AssetValueChangedEvent;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PriceSyncSchedulerTest {

    @Mock private MarketService marketService;
    @Mock private AssetRepository assetRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PriceSyncScheduler scheduler;

    private User testUser;
    private Asset goldAsset;
    private Asset stockAsset;
    private MarketPrice goldPrice;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("jan@example.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());

        goldAsset = Asset.builder()
                .user(testUser)
                .name("Złoto 10g")
                .assetType(AssetType.GOLD)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("380.00"))
                .currentValue(new BigDecimal("1500.00"))
                .currency("PLN")
                .build();
        ReflectionTestUtils.setField(goldAsset, "id", UUID.randomUUID());

        stockAsset = Asset.builder()
                .user(testUser)
                .name("CD Projekt")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("180.00"))
                .currentValue(new BigDecimal("2600.00"))
                .currency("PLN")
                .externalId("CDR.PL")
                .build();
        ReflectionTestUtils.setField(stockAsset, "id", UUID.randomUUID());

        goldPrice = MarketPrice.builder()
                .symbol("XAU")
                .price(new BigDecimal("200.00"))
                .currency("PLN")
                .source("GOLD_API")
                .fetchedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should update gold assets when price is available")
    void shouldUpdateGoldAssetsWhenPriceAvailable() {
        // given
        given(assetRepository.findByAssetType(AssetType.GOLD))
                .willReturn(List.of(goldAsset));
        given(marketService.getCurrentPrice("XAU"))
                .willReturn(Optional.of(goldPrice));
        given(assetRepository.save(any(Asset.class)))
                .willReturn(goldAsset);

        // when
        scheduler.syncGoldPrices();

        // then
        assertThat(goldAsset.getCurrentValue())
                .isEqualByComparingTo(new BigDecimal("2000.00"));
        then(assetRepository).should().save(goldAsset);
    }

    @Test
    @DisplayName("Should publish event when gold price changes")
    void shouldPublishEventWhenGoldPriceChanges() {
        // given
        given(assetRepository.findByAssetType(AssetType.GOLD))
                .willReturn(List.of(goldAsset));
        given(marketService.getCurrentPrice("XAU"))
                .willReturn(Optional.of(goldPrice));
        given(assetRepository.save(any())).willReturn(goldAsset);

        // when
        scheduler.syncGoldPrices();

        // then
        then(eventPublisher).should()
                .publishEvent(any(AssetValueChangedEvent.class));
    }

    @Test
    @DisplayName("Should skip sync when no gold assets")
    void shouldSkipSyncWhenNoGoldAssets() {
        // given
        given(assetRepository.findByAssetType(AssetType.GOLD))
                .willReturn(List.of());

        // when
        scheduler.syncGoldPrices();

        // then
        then(marketService).should(never()).getCurrentPrice(any());
        then(assetRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should skip sync when market price unavailable")
    void shouldSkipSyncWhenMarketPriceUnavailable() {
        // given
        given(assetRepository.findByAssetType(AssetType.GOLD))
                .willReturn(List.of(goldAsset));
        given(marketService.getCurrentPrice("XAU"))
                .willReturn(Optional.empty());

        // when
        scheduler.syncGoldPrices();

        // then
        then(assetRepository).should(never()).save(any());
        then(eventPublisher).should(never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should skip stock asset without externalId")
    void shouldSkipStockAssetWithoutExternalId() {
        // given
        stockAsset.setExternalId(null);

        given(assetRepository.findByAssetType(AssetType.STOCK))
                .willReturn(List.of(stockAsset));

        // when
        scheduler.syncStockPrices();

        // then
        then(marketService).should(never()).getCurrentPrice(any());
        then(assetRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should update stock asset with externalId")
    void shouldUpdateStockAssetWithExternalId() {
        // given
        MarketPrice stockPrice = MarketPrice.builder()
                .symbol("CDR.PL")
                .price(new BigDecimal("270.00"))
                .currency("PLN")
                .source("STOOQ")
                .fetchedAt(Instant.now())
                .build();

        given(assetRepository.findByAssetType(AssetType.STOCK))
                .willReturn(List.of(stockAsset));
        given(marketService.getCurrentPrice("CDR.PL"))
                .willReturn(Optional.of(stockPrice));
        given(assetRepository.save(any())).willReturn(stockAsset);

        // when
        scheduler.syncStockPrices();

        // then
        assertThat(stockAsset.getCurrentValue())
                .isEqualByComparingTo(new BigDecimal("2700.00"));
        then(assetRepository).should().save(stockAsset);
    }
}
