package com.speedster.investment.smart_investment_platform.asset.application.service;

import com.speedster.investment.smart_investment_platform.asset.application.dto.AssetResponse;
import com.speedster.investment.smart_investment_platform.asset.application.dto.CreateAssetRequest;
import com.speedster.investment.smart_investment_platform.asset.application.dto.UpdateAssetRequest;
import com.speedster.investment.smart_investment_platform.asset.application.dto.UpdateAssetValueRequest;
import com.speedster.investment.smart_investment_platform.asset.application.mapper.AssetMapper;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.market.application.MarketService;
import com.speedster.investment.smart_investment_platform.shared.event.AssetValueChangedEvent;
import com.speedster.investment.smart_investment_platform.shared.exception.BusinessException;
import com.speedster.investment.smart_investment_platform.shared.exception.ResourceNotFoundException;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetMapper assetMapper;
    private final AssetOwnershipGuard ownershipGuard;
    private final ApplicationEventPublisher eventPublisher;

    public AssetResponse createAsset(UUID userId, CreateAssetRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userId.toString()
                ));

        Asset asset = Asset.builder()
                .user(user)
                .name(request.name())
                .assetType(request.assetType())
                .quantity(request.quantity())
                .purchasePrice(request.purchasePrice())
                .currency(request.currency())
                .externalId(request.externalId())
                .notes(request.notes())
                .build();

        return assetMapper.toResponse(assetRepository.save(asset));
    }

    public List<AssetResponse> getUserAssets(UUID userId){
        return assetMapper.toResponseList(
                assetRepository.findByUserId(userId)
        );
    }

    public AssetResponse getAsset(UUID assetId, UUID userId) {
        return assetMapper.toResponse(
                ownershipGuard.getAssetForUser(assetId, userId));
    }

    public AssetResponse updateAsset(UUID assetId, UUID userId, UpdateAssetRequest request){
        Asset asset = ownershipGuard.getAssetForUser(assetId, userId);

        if (request.name() != null) asset.setName(request.name());
        if (request.quantity() != null) asset.setQuantity(request.quantity());
        if (request.purchasePrice() != null) asset.setPurchasePrice(request.purchasePrice());
        if (request.notes() != null) asset.setNotes(request.notes());

        return assetMapper.toResponse(assetRepository.save(asset));
    }

    public void deleteAsset(UUID assetId, UUID userId){
        ownershipGuard.getAssetForUser(assetId, userId);
        assetRepository.deleteById(assetId);
    }

    public AssetResponse updateCurrentValue(UUID assetId, UUID userId,
                                            UpdateAssetValueRequest request) {

        Asset asset = ownershipGuard.getAssetForUser(assetId, userId);

        if (asset.getAssetType() != AssetType.LEGO) {
            throw new BusinessException(
                    "Manual value update is only allowed for LEGO assets. " +
                            "Gold and stocks are updated automatically.",
                    HttpStatus.BAD_REQUEST
            );
        }

        BigDecimal previousValue = asset.getCurrentValue();
        asset.setCurrentValue(request.currentValue());
        assetRepository.save(asset);

        if (previousValue != null) {
            publishValueChangedEvent(asset, previousValue, request.currentValue());
        }

        return assetMapper.toResponse(asset);
    }

    private void publishValueChangedEvent(Asset asset,
                                          BigDecimal previousValue, BigDecimal newValue) {
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
