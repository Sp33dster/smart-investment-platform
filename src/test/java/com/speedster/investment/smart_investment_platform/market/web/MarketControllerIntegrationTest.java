package com.speedster.investment.smart_investment_platform.market.web;

import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPriceRepository;
import com.speedster.investment.smart_investment_platform.market.infrastructure.persistance.JpaMarketPriceRepository;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;

@Testcontainers
class MarketControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketPriceRepository marketPriceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        ((JpaMarketPriceRepository) marketPriceRepository).deleteAll();
        ((JpaUserRepository) userRepository).deleteAll();

        User user = User.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        userRepository.save(user);
        userToken = jwtService.generateToken(user);

        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Admin")
                .lastName("Admin")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        adminToken = jwtService.generateToken(admin);
    }

    @Test
    @DisplayName("Should return 404 when no price stored for symbol")
    void shouldReturn404WhenNoPriceStored() throws Exception {
        mockMvc.perform(get("/api/v1/market/price/XAU/latest")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return latest stored price")
    void shouldReturnLatestStoredPrice() throws Exception {
        // given
        MarketPrice price = MarketPrice.builder()
                .symbol("XAU")
                .price(new BigDecimal("16000.00"))
                .currency("PLN")
                .source("GOLD_API")
                .fetchedAt(Instant.now())
                .build();
        marketPriceRepository.save(price);

        // when & then
        mockMvc.perform(get("/api/v1/market/price/XAU/latest")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("XAU"))
                .andExpect(jsonPath("$.price").value(16000.00))
                .andExpect(jsonPath("$.source").value("GOLD_API"));
    }

    @Test
    @DisplayName("Should return 403 when USER tries to trigger sync")
    void shouldReturn403WhenUserTriggersSync() throws Exception {
        mockMvc.perform(post("/api/v1/market/sync/gold")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when no token for protected endpoint")
    void shouldReturn403WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/market/price/XAU/latest"))
                .andExpect(status().isForbidden());
    }
}
