package com.speedster.investment.smart_investment_platform.asset.aplication;

import com.speedster.investment.smart_investment_platform.asset.application.dto.PortfolioSummaryResponse;
import com.speedster.investment.smart_investment_platform.asset.application.mapper.AssetMapper;
import com.speedster.investment.smart_investment_platform.asset.application.service.PortfolioService;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private AssetRepository assetRepository;
    @Mock private AssetMapper assetMapper;

    @InjectMocks
    private PortfolioService portfolioService;

    private User testUser;
    private Asset legoAsset;
    private Asset goldAsset;
    private Asset stockAsset;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("jan@example.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());

        legoAsset = Asset.builder()
                .user(testUser)
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("850.00"))
                .currentValue(new BigDecimal("1200.00"))
                .currency("PLN")
                .build();

        goldAsset = Asset.builder()
                .user(testUser)
                .name("Złoto 10g")
                .assetType(AssetType.GOLD)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("380.00"))
                .currentValue(new BigDecimal("5000.00"))
                .currency("PLN")
                .build();

        stockAsset = Asset.builder()
                .user(testUser)
                .name("CD Projekt")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("180.00"))
                .currentValue(new BigDecimal("2600.00"))
                .currency("PLN")
                .build();
    }

    @Test
    @DisplayName("Should calculate total purchase value correctly")
    void shouldCalculateTotalPurchaseValueCorrectly() {
        // given
        UUID userId = testUser.getId();
        given(assetRepository.findByUserId(userId))
                .willReturn(List.of(legoAsset, goldAsset));
        given(assetMapper.toResponseList(any()))
                .willReturn(List.of());

        // when
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        // then
        assertThat(summary.totalPurchaseValue())
                .isEqualByComparingTo(new BigDecimal("4650.00"));
    }

    @Test
    @DisplayName("Should calculate total current value correctly")
    void shouldCalculateTotalCurrentValueCorrectly() {
        // given
        UUID userId = testUser.getId();
        given(assetRepository.findByUserId(userId))
                .willReturn(List.of(legoAsset, goldAsset));
        given(assetMapper.toResponseList(any()))
                .willReturn(List.of());

        // when
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        // then
        assertThat(summary.totalCurrentValue())
                .isEqualByComparingTo(new BigDecimal("6200.00"));
    }

    @Test
    @DisplayName("Should use purchase value when current value is null")
    void shouldUsePurchaseValueWhenCurrentValueIsNull() {
        // given
        UUID userId = testUser.getId();
        legoAsset.setCurrentValue(null);

        given(assetRepository.findByUserId(userId))
                .willReturn(List.of(legoAsset));
        given(assetMapper.toResponseList(any()))
                .willReturn(List.of());

        // when
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        // then
        assertThat(summary.totalCurrentValue())
                .isEqualByComparingTo(new BigDecimal("850.00"));
        assertThat(summary.totalPurchaseValue())
                .isEqualByComparingTo(new BigDecimal("850.00"));
    }

    @Test
    @DisplayName("Should calculate total gain loss correctly")
    void shouldCalculateTotalGainLossCorrectly() {
        // given
        UUID userId = testUser.getId();
        given(assetRepository.findByUserId(userId))
                .willReturn(List.of(legoAsset, goldAsset));
        given(assetMapper.toResponseList(any()))
                .willReturn(List.of());

        // when
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        // then
        assertThat(summary.totalGainLoss())
                .isEqualByComparingTo(new BigDecimal("1550.00"));
    }

    @Test
    @DisplayName("Should calculate gain loss percent correctly")
    void shouldCalculateGainLossPercentCorrectly() {
        // given
        UUID userId = testUser.getId();
        given(assetRepository.findByUserId(userId))
                .willReturn(List.of(legoAsset, goldAsset));
        given(assetMapper.toResponseList(any()))
                .willReturn(List.of());

        // when
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        // then
        assertThat(summary.totalGainLossPercent())
                .isEqualByComparingTo(new BigDecimal("33.3300"));
    }

    @Test
    @DisplayName("Should group value by asset type correctly")
    void shouldGroupValueByAssetTypeCorrectly() {
        // given
        UUID userId = testUser.getId();
        given(assetRepository.findByUserId(userId))
                .willReturn(List.of(legoAsset, goldAsset, stockAsset));
        given(assetMapper.toResponseList(any()))
                .willReturn(List.of());

        // when
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        // then
        assertThat(summary.valueByType())
                .containsEntry(AssetType.LEGO, new BigDecimal("1200.00"))
                .containsEntry(AssetType.GOLD, new BigDecimal("5000.00"))
                .containsEntry(AssetType.STOCK, new BigDecimal("2600.00"));
    }

    @Test
    @DisplayName("Should return zero percent when total purchase is zero")
    void shouldReturnZeroPercentWhenTotalPurchaseIsZero() {
        // given
        UUID userId = testUser.getId();
        given(assetRepository.findByUserId(userId))
                .willReturn(List.of());
        given(assetMapper.toResponseList(any()))
                .willReturn(List.of());

        // when
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        // then
        assertThat(summary.totalGainLossPercent())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalPurchaseValue())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}
