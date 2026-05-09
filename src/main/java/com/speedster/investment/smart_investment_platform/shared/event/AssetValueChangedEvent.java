package com.speedster.investment.smart_investment_platform.shared.event;

import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssetValueChangedEvent(
        UUID assetId,
        UUID userId,
        String assetName,
        AssetType assetType,
        BigDecimal previousValue,
        BigDecimal currentValue,
        BigDecimal changePercent,
        Instant occurredAt
) implements DomainEvent {
}
