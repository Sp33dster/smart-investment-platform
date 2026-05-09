package com.speedster.investment.smart_investment_platform.market.application;

import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
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
    public void syncGoldPrices() {
        log.info("Starting gold price sync...");
        syncAssetsBySymbol(AssetType.GOLD, "XAU");
    }

    @Scheduled(cron = "0 30 8,17 * * MON-FRI")
    public void syncStockPrices() {
        log.info("Starting stock price sync...");

        List<Asset> stockAssets = assetRepository.findByAssetType(AssetType.STOCK);

        if (stockAssets.isEmpty()) {
            log.info("No stock assets found, skipping sync");
            return;
        }

        stockAssets.stream()
                .filter(this::hasExternalId)
                .forEach(this::syncSingleStockAsset);
    }

    private void syncAssetsBySymbol(AssetType type, String symbol) {
        List<Asset> assets = assetRepository.findByAssetType(type);

        if (assets.isEmpty()) {
            log.info("No {} assets found, skipping sync", type);
            return;
        }

        marketService.getCurrentPrice(symbol).ifPresentOrElse(
                price -> {
                    assets.forEach(asset -> updateAssetValue(asset, price));
                    log.info("Updated {} {} assets with price {} PLN",
                            assets.size(), type, price.getPrice());
                },
                () -> log.warn("Failed to fetch {} price, skipping sync", type)
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

    private boolean hasExternalId(Asset asset) {
        return asset.getExternalId() != null
                && !asset.getExternalId().isBlank();
    }

    private void syncSingleStockAsset(Asset asset) {
        marketService.getCurrentPrice(asset.getExternalId())
                .ifPresentOrElse(
                        price -> updateAssetValue(asset, price),
                        () -> log.warn("No price found for {} ({})",
                                asset.getName(), asset.getExternalId())
                );
    }

    private void updateAssetValue(Asset asset, MarketPrice price) {
        BigDecimal previousValue = asset.getCurrentValue();
        BigDecimal newValue = price.getPrice().multiply(asset.getQuantity());

        asset.setCurrentValue(newValue);
        assetRepository.save(asset);

        log.info("Updated {} → {} PLN", asset.getName(), newValue);

        if (previousValue != null) {
            publishValueChangedEvent(asset, previousValue, newValue);
        }
    }
}
