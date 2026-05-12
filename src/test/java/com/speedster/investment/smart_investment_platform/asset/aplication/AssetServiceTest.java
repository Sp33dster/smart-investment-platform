package com.speedster.investment.smart_investment_platform.asset.aplication;

import com.speedster.investment.smart_investment_platform.asset.application.dto.AssetResponse;
import com.speedster.investment.smart_investment_platform.asset.application.dto.CreateAssetRequest;
import com.speedster.investment.smart_investment_platform.asset.application.dto.UpdateAssetValueRequest;
import com.speedster.investment.smart_investment_platform.asset.application.mapper.AssetMapper;
import com.speedster.investment.smart_investment_platform.asset.application.service.AssetOwnershipGuard;
import com.speedster.investment.smart_investment_platform.asset.application.service.AssetService;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.shared.event.AssetValueChangedEvent;
import com.speedster.investment.smart_investment_platform.shared.exception.BusinessException;
import com.speedster.investment.smart_investment_platform.shared.exception.ResourceNotFoundException;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    @Mock private AssetRepository assetRepository;
    @Mock private UserRepository userRepository;
    @Mock private AssetMapper assetMapper;
    @Mock private AssetOwnershipGuard ownershipGuard;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AssetService assetService;

    private User testUser;
    private Asset legoAsset;
    private Asset goldAsset;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("jan@example.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());

        legoAsset = Asset.builder()
                .user(testUser)
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("850.00"))
                .currency("PLN")
                .build();
        ReflectionTestUtils.setField(legoAsset, "id", UUID.randomUUID());

        goldAsset = Asset.builder()
                .user(testUser)
                .name("Złoto 10g")
                .assetType(AssetType.GOLD)
                .quantity(new BigDecimal("10"))
                .purchasePrice(new BigDecimal("380.00"))
                .currency("PLN")
                .build();
        ReflectionTestUtils.setField(goldAsset, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Should create asset successfully")
    void shouldCreateAssetSuccessfully(){
        //given
        UUID userId = testUser.getId();
        CreateAssetRequest request = new CreateAssetRequest(
                "LEGO Millenium Falcon", AssetType.LEGO, new BigDecimal("1"), new BigDecimal("850.00"),
                "PLN", null, "Sealed box");

        given(userRepository.findById(userId))
                .willReturn(Optional.of(testUser));
        given(assetRepository.save(any(Asset.class)))
                .willReturn(legoAsset);
        given(assetMapper.toResponse(legoAsset))
                .willReturn(mock(AssetResponse.class));

        //when
        assetService.createAsset(userId, request);

        //then
        then(assetRepository).should().save(any(Asset.class));
    }

    @Test
    @DisplayName("Should throw when user not found during asset creation")
    void shouldThrowWhenUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        CreateAssetRequest request = new CreateAssetRequest(
                "LEGO", AssetType.LEGO,
                new BigDecimal("1"), new BigDecimal("850"),
                "PLN", null, null);

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assetService.createAsset(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        then(assetRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should allow manual value update for LEGO asset")
    void shouldAllowManualValueUpdateForLegoAsset() {
        // given
        UUID assetId = legoAsset.getId();
        UUID userId = testUser.getId();
        UpdateAssetValueRequest request =
                new UpdateAssetValueRequest(new BigDecimal("1200.00"));

        given(ownershipGuard.getAssetForUser(assetId, userId))
                .willReturn(legoAsset);
        given(assetRepository.save(legoAsset)).willReturn(legoAsset);
        given(assetMapper.toResponse(legoAsset)).willReturn(mock(AssetResponse.class));

        // when
        assetService.updateCurrentValue(assetId, userId, request);

        // then
        assertThat(legoAsset.getCurrentValue())
                .isEqualByComparingTo(new BigDecimal("1200.00"));
        then(assetRepository).should().save(legoAsset);
    }

    @Test
    @DisplayName("Should throw when updating value for GOLD asset")
    void shouldThrowWhenUpdatingValueForGoldAsset() {
        // given
        UUID assetId = goldAsset.getId();
        UUID userId = testUser.getId();
        UpdateAssetValueRequest request =
                new UpdateAssetValueRequest(new BigDecimal("1000"));

        given(ownershipGuard.getAssetForUser(assetId, userId))
                .willReturn(goldAsset);

        // when & then
        assertThatThrownBy(() ->
                assetService.updateCurrentValue(assetId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Manual value update is only allowed for LEGO");

        then(assetRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should publish event when updating LEGO value with previous value")
    void shouldPublishEventWhenUpdatingLegoValue() {
        // given
        legoAsset.setCurrentValue(new BigDecimal("1000.00")); // poprzednia wartość
        UUID assetId = legoAsset.getId();
        UUID userId = testUser.getId();
        UpdateAssetValueRequest request =
                new UpdateAssetValueRequest(new BigDecimal("1200.00"));

        given(ownershipGuard.getAssetForUser(assetId, userId))
                .willReturn(legoAsset);
        given(assetRepository.save(legoAsset)).willReturn(legoAsset);
        given(assetMapper.toResponse(legoAsset)).willReturn(mock(AssetResponse.class));

        // when
        assetService.updateCurrentValue(assetId, userId, request);

        // then
        then(eventPublisher).should().publishEvent(any(AssetValueChangedEvent.class));
    }
}
