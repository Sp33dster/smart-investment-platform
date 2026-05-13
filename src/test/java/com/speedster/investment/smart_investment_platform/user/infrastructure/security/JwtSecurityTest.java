package com.speedster.investment.smart_investment_platform.user.infrastructure.security;

import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtSecurityTest {

    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService,"expiration", 86400000L);

        testUser = User.builder()
                .email("jan@example.com")
                .password("hashedPassword")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken(){
        //given
        String token = jwtService.generateToken(testUser);

        //when
        String extractedEmail = jwtService.extractEmail(token);

        //then
        assertThat(extractedEmail).isEqualTo("jan@example.com");
    }

    @Test
    @DisplayName("Should validate token for correct user")
    void shouldValidateTokenForCorrectUser() {
        // given
        String token = jwtService.generateToken(testUser);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username("jan@example.com")
                .password("hashedPassword")
                .roles("USER")
                .build();

        // when
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject token for different user")
    void shouldRejectTokenForDifferentUser() {
        // given
        String token = jwtService.generateToken(testUser);
        UserDetails differentUser = org.springframework.security.core.userdetails.User
                .builder()
                .username("anna@example.com")
                .password("hashedPassword")
                .roles("USER")
                .build();

        // when
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate token with correct email as subject")
    void shouldGenerateTokenWithCorrectSubject() {
        // given & when
        String token = jwtService.generateToken(testUser);

        // then
        assertThat(jwtService.extractEmail(token))
                .isEqualTo("jan@example.com");
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        // given
        ReflectionTestUtils.setField(jwtService, "expiration", -1L);
        String expiredToken = jwtService.generateToken(testUser);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username("jan@example.com")
                .password("hashedPassword")
                .roles("USER")
                .build();

        // when
        boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate token with role claim")
    void shouldGenerateTokenWithRoleClaim() {
        // given & when
        String token = jwtService.generateToken(testUser);

        // then
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(token).isNotBlank();
    }

}
