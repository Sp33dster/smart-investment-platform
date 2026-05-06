package com.speedster.investment.smart_investment_platform.asset.infrastructure.persistance;

import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaAssetRepository extends JpaRepository<Asset, UUID>, AssetRepository {
}
