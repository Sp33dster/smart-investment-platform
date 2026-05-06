package com.speedster.investment.smart_investment_platform.asset.application;

import com.speedster.investment.smart_investment_platform.asset.application.mapper.AssetMapper;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.asset.infrastructure.persistance.JpaAssetRepository;
import com.speedster.investment.smart_investment_platform.market.application.MarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceSyncScheduler {

    private final MarketService marketService;
    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    @Scheduled(cron = "0 0 8,20 * * *")
    public void syncGoldPrices(){
        log.info("Starting gold price sync...");

        List<Asset> goldAssets = assetRepository.findByAssetType(AssetType.GOLD);

        if (goldAssets.isEmpty()){
            log.info("No gold assets found, skipping sync");
        }

        marketService.getCurrentPrice("XAU").ifPresentOrElse(
                price -> {
                    goldAssets.forEach(asset -> {
                        BigDecimal currentValue = price.getPrice()
                                .multiply(asset.getQuantity());
                        asset.setCurrentValue(currentValue);
                    });

                    ((JpaAssetRepository) assetRepository).saveAll(goldAssets);

                    log.info("Updated {} gold assets with price {} PLN", goldAssets.size(),
                            price.getPrice());
                },
                () -> log.warn("Failed to fetch gols price, skipping sync")
        );
    }
}
