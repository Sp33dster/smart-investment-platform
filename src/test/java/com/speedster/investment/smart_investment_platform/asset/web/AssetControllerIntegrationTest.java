package com.speedster.investment.smart_investment_platform.asset.web;

import com.speedster.investment.smart_investment_platform.asset.application.dto.CreateAssetRequest;
import com.speedster.investment.smart_investment_platform.asset.application.dto.UpdateAssetValueRequest;
import com.speedster.investment.smart_investment_platform.asset.domain.Asset;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetRepository;
import com.speedster.investment.smart_investment_platform.asset.domain.AssetType;
import com.speedster.investment.smart_investment_platform.asset.infrastructure.persistance.JpaAssetRepository;
import com.speedster.investment.smart_investment_platform.shared.AbstractIntegrationTest;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import com.speedster.investment.smart_investment_platform.user.infrastructure.persistance.JpaUserRepository;
import com.speedster.investment.smart_investment_platform.user.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@Testcontainers
class AssetControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        ((JpaAssetRepository) assetRepository).deleteAll();
        ((JpaUserRepository) userRepository).deleteAll();

        testUser = User.builder()
                .email("jan@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        jwtToken = jwtService.generateToken(testUser);
    }

    @Test
    @DisplayName("Should create asset and return 201")
    void shouldCreateAssetAndReturn201() throws Exception {
        // given
        CreateAssetRequest request = new CreateAssetRequest(
                "LEGO Millennium Falcon", AssetType.LEGO,
                new BigDecimal("1"), new BigDecimal("850.00"),
                "PLN", null, "Sealed box");

        // when & then
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("LEGO Millennium Falcon"))
                .andExpect(jsonPath("$.assetType").value("LEGO"))
                .andExpect(jsonPath("$.purchasePrice").value("850.00"));
    }

    @Test
    @DisplayName("Should return 403 when no token")
    void shouldReturn403WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/assets"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return empty list when no assets")
    void shouldReturnEmptyListWhenNoAssets() throws Exception {
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return assets for current user")
    void shouldReturnAssetsForCurrentUser() throws Exception {
        // given
        Asset asset = Asset.builder()
                .user(testUser)
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("850.00"))
                .currency("PLN")
                .build();
        assetRepository.save(asset);

        // when & then
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("LEGO Millennium Falcon"));
    }

    @Test
    @DisplayName("Should delete asset and return 204")
    void shouldDeleteAssetAndReturn204() throws Exception {
        // given
        Asset asset = Asset.builder()
                .user(testUser)
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("850.00"))
                .currency("PLN")
                .build();
        Asset saved = assetRepository.save(asset);

        // when & then
        mockMvc.perform(delete("/api/v1/assets/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 403 when deleting another user asset")
    void shouldReturn403WhenDeletingAnotherUserAsset() throws Exception {
        // given — inny użytkownik
        User otherUser = User.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Anna")
                .lastName("Nowak")
                .role(Role.USER)
                .build();
        userRepository.save(otherUser);

        Asset otherAsset = Asset.builder()
                .user(otherUser)
                .name("LEGO Set")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("500.00"))
                .currency("PLN")
                .build();
        assetRepository.save(otherAsset);

        // when & then
        mockMvc.perform(delete("/api/v1/assets/" + otherAsset.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update LEGO value manually")
    void shouldUpdateLegoValueManually() throws Exception {
        // given
        Asset asset = Asset.builder()
                .user(testUser)
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("850.00"))
                .currency("PLN")
                .build();
        Asset saved = assetRepository.save(asset);

        UpdateAssetValueRequest request =
                new UpdateAssetValueRequest(new BigDecimal("1200.00"));

        // when & then
        mockMvc.perform(patch("/api/v1/assets/" + saved.getId() + "/value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentValue").value("1200.00"));
    }

    @Test
    @DisplayName("Should return portfolio summary")
    void shouldReturnPortfolioSummary() throws Exception {
        // given
        Asset asset = Asset.builder()
                .user(testUser)
                .name("LEGO Millennium Falcon")
                .assetType(AssetType.LEGO)
                .quantity(new BigDecimal("1"))
                .purchasePrice(new BigDecimal("850.00"))
                .currentValue(new BigDecimal("1200.00"))
                .currency("PLN")
                .build();
        assetRepository.save(asset);

        // when & then
        mockMvc.perform(get("/api/v1/assets/summary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPurchaseValue").value("850.00"))
                .andExpect(jsonPath("$.totalCurrentValue").value("1200.00"))
                .andExpect(jsonPath("$.totalGainLoss").value("350.00"));
    }
}
