package com.speedster.investment.smart_investment_platform.market.application;

import com.speedster.investment.smart_investment_platform.asset.application.mapper.AssetMapper;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.asset.infrastructure.persistance.JpaAssetRepository;
import com.speedster.investment.smart_investment_platform.shared.event.AssetValueChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceSyncScheduler {

    private final MarketService marketService;
    private final AssetRepository assetRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 8,20 * * *")
    public void syncGoldPrices(){
        log.info("Starting gold price sync...");

        List<Asset> goldAssets = assetRepository.findByAssetType(AssetType.GOLD);

        if (goldAssets.isEmpty()){
            log.info("No gold assets found, skipping sync");
            return;
        }

        marketService.getCurrentPrice("XAU").ifPresentOrElse(
                price -> {
                    goldAssets.forEach(asset -> {

                        BigDecimal previousValue = asset.getCurrentValue();
                        log.info("Asset: {}, previousValue: {}", asset.getName(), previousValue);
                        BigDecimal newValue = price.getPrice()
                                .multiply(asset.getQuantity());

                        asset.setCurrentValue(newValue);
                        assetRepository.save(asset);

                        if (previousValue != null) {
                            publishValueChangedEvent(asset, previousValue, newValue);
                        }
                    });

                    log.info("Updated {} gold assets with price {} PLN",
                            goldAssets.size(), price.getPrice());
                },
                () -> log.warn("Failed to fetch gold price, skipping sync")
        );
    }

    private void publishValueChangedEvent(Asset asset, BigDecimal previousValue,
                                          BigDecimal newValue){
        BigDecimal changePercent = newValue.subtract(previousValue)
                .divide(previousValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        eventPublisher.publishEvent(new AssetValueChangedEvent(
                asset.getId(),
                asset.getUser().getId(),
                asset.getName(),
                asset.getAssetType(),
                previousValue,
                newValue,
                changePercent,
                Instant.now()
        ));
    }
}
