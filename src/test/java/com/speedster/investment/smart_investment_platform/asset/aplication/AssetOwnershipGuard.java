package com.speedster.investment.smart_investment_platform.asset.aplication;

import com.speedster.investment.smart_investment_platform.asset.application.service.AssetOwnershipGuard;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.shared.exception.BusinessException;
import com.speedster.investment.smart_investment_platform.shared.exception.ResourceNotFoundException;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssetOwnershipGuardTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetOwnershipGuard ownershipGuard;

    private User owner;
    private User otherUser;
    private Asset asset;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .email("owner@example.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());

        otherUser = User.builder()
                .email("other@example.com")
                .firstName("Anna")
                .lastName("Nowak")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(otherUser, "id", UUID.randomUUID());

        asset = Asset.builder()
                .user(owner)
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("850.00"))
                .currency("PLN")
                .build();
        ReflectionTestUtils.setField(asset, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Should return asset when user is the owner")
    void shouldReturnAssetWhenUserIsOwner() {
        // given
        UUID assetId = asset.getId();
        UUID ownerId = owner.getId();

        given(assetRepository.findById(assetId))
                .willReturn(Optional.of(asset));

        // when
        Asset result = ownershipGuard.getAssetForUser(assetId, ownerId);

        // then
        assertThat(result).isEqualTo(asset);
        assertThat(result.getUser().getId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset not found")
    void shouldThrowWhenAssetNotFound() {
        // given
        UUID assetId = UUID.randomUUID();
        UUID userId = owner.getId();

        given(assetRepository.findById(assetId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                ownershipGuard.getAssetForUser(assetId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw BusinessException when user is not the owner")
    void shouldThrowWhenUserIsNotOwner() {
        // given
        UUID assetId = asset.getId();
        UUID otherUserId = otherUser.getId();

        given(assetRepository.findById(assetId))
                .willReturn(Optional.of(asset));

        // when & then
        assertThatThrownBy(() ->
                ownershipGuard.getAssetForUser(assetId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("Should throw BusinessException with FORBIDDEN status")
    void shouldThrowWithForbiddenStatus() {
        // given
        UUID assetId = asset.getId();
        UUID otherUserId = otherUser.getId();

        given(assetRepository.findById(assetId))
                .willReturn(Optional.of(asset));

        // when & then
        assertThatThrownBy(() ->
                ownershipGuard.getAssetForUser(assetId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(
                        ((BusinessException) ex).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }
}
