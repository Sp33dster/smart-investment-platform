package com.speedster.investment.smart_investment_platform.asset.web;

import com.speedster.investment.smart_investment_platform.asset.application.dto.AssetResponse;
import com.speedster.investment.smart_investment_platform.asset.application.dto.CreateAssetRequest;
import com.speedster.investment.smart_investment_platform.asset.application.dto.PortfolioSummaryResponse;
import com.speedster.investment.smart_investment_platform.asset.application.dto.UpdateAssetRequest;
import com.speedster.investment.smart_investment_platform.asset.application.service.AssetService;
import com.speedster.investment.smart_investment_platform.asset.application.service.PortfolioService;
import com.speedster.investment.smart_investment_platform.shared.exception.ResourceNotFoundException;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Investment portfolio management")
public class AssetController {

    private final AssetService assetService;
    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId(Authentication authentication){
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email))
                .getId();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add new asset to portfolio")
    public AssetResponse createAsset(
            @Valid @RequestBody CreateAssetRequest request,
            Authentication authentication) {

        UUID userId = getCurrentUserId(authentication);
        return assetService.createAsset(userId, request);
    }

    @GetMapping
    @Operation(summary = "Get all assets for current user")
    public List<AssetResponse> getUserAssets(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        return assetService.getUserAssets(userId);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get portfolio summary with totals")
    public PortfolioSummaryResponse getPortfolioSummary(
            Authentication authentication) {
        return portfolioService.getSummary(getCurrentUserId(authentication));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single asset by ID")
    public AssetResponse getAsset(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = getCurrentUserId(authentication);
        return assetService.getAsset(id, userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset")
    public AssetResponse updateAsset(
            @PathVariable UUID id,
            @RequestBody UpdateAssetRequest request,
            Authentication authentication) {

        UUID userId = getCurrentUserId(authentication);
        return assetService.updateAsset(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete asset")
    public void deleteAsset(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = getCurrentUserId(authentication);
        assetService.deleteAsset(id, userId);
    }

}
