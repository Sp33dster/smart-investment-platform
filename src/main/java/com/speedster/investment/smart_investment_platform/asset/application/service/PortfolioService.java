package com.speedster.investment.smart_investment_platform.asset.application.service;

import com.speedster.investment.smart_investment_platform.asset.application.dto.PortfolioSummaryResponse;
import com.speedster.investment.smart_investment_platform.asset.application.mapper.AssetMapper;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public PortfolioSummaryResponse getSummary(UUID userId){
        List<Asset> assets = assetRepository.findByUserId(userId);

        BigDecimal totalPurchase = calculateToralPurchase(assets);
        BigDecimal totalCurrent = calculateTotalCurrent(assets);
        BigDecimal totalGainLoss = totalCurrent.subtract(totalPurchase)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalGainLossPercentage = calculatePercent(totalGainLoss, totalPurchase);
        Map<AssetType, BigDecimal> valueByType = calculateByType(assets);

        return new PortfolioSummaryResponse(
                assetMapper.toResponseList(assets),
                totalPurchase,
                totalCurrent,
                totalGainLoss,
                totalGainLossPercentage,
                valueByType
        );
    }

    private BigDecimal calculateToralPurchase(List<Asset> assets){
        return assets.stream()
                .map(a -> a.getPurchasePrice().multiply(a.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalCurrent(List<Asset> assets){
        return assets.stream()
                .map(a -> a.getCurrentValue() != null
                ? a.getCurrentValue()
                : a.getPurchasePrice().multiply(a.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercent(BigDecimal gainLoss, BigDecimal totalPurchase){
        if (totalPurchase.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        return gainLoss
                .divide(totalPurchase, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2,RoundingMode.HALF_UP);
    }

    private Map<AssetType, BigDecimal> calculateByType(List<Asset> assets){
        return assets.stream()
                .collect(Collectors.groupingBy(
                        Asset::getAssetType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                a -> a.getCurrentValue() != null
                                        ? a.getCurrentValue()
                                        : a.getPurchasePrice().multiply(a.getQuantity()),
                                BigDecimal::add
                        )
                ));
    }
}
