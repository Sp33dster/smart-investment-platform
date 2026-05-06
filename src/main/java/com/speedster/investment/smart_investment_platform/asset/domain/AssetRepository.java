package com.speedster.investment.smart_investment_platform.asset.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository {
    Asset save(Asset asset);
    Optional<Asset> findById(UUID id);
    List<Asset> findByUserId(UUID userId);
    List<Asset> findByUserIdAndAssetType(UUID userId, AssetType assetType);
    void deleteById(UUID id);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
