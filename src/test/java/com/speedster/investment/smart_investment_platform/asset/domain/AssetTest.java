package com.speedster.investment.smart_investment_platform.asset.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

public class AssetTest {

    private Asset asset;

    @BeforeEach
    void setUp(){
        asset = Asset.builder()
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("2"))
                .purchasePrice(new BigDecimal("850.00"))
                .currency("PLN")
                .build();
    }

    @Test
    @DisplayName("Should return zero gain loss when current value is null")
    void shouldReturnZeroWhenCurrentValueIsNull(){
        assertThat(asset.calculateGainLoss())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate positive gain loss correctly")
    void shouldCalculatePositiveGainLoss(){
        //given
        asset.setCurrentValue(new BigDecimal("2000.00"));

        //when
        BigDecimal gainLoss = asset.calculateGainLoss();

        //then
        assertThat(gainLoss)
                .isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("Should create negative gain loss correctly")
    void shouldCalculateNegativeGainLoss(){
        //given
        asset.setCurrentValue(new BigDecimal("1000.00"));

        //when
        BigDecimal gainloss = asset.calculateGainLoss();

        //then
        assertThat(gainloss)
                .isEqualByComparingTo(new BigDecimal("-700.00"));
    }

    @Test
    @DisplayName("Should return zero percent when current value is null")
    void shouldReturnZeroPercentWhenCurrentValueIsNull(){
        assertThat(asset.calculateGainLossPercent())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}
