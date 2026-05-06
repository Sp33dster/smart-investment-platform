package com.speedster.investment.smart_investment_platform.asset.application.service;

import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.shared.exception.BusinessException;
import com.speedster.investment.smart_investment_platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AssetOwnershipGuard {

    private final AssetRepository assetRepository;

    public Asset getAssetForUser(UUID assetId, UUID userId){
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset", assetId.toString()
                ));
        if (!asset.getUser().getId().equals(userId)){
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }
        return asset;
    }
}
