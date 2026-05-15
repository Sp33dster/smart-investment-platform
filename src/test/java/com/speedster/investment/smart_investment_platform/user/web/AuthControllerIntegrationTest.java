package com.speedster.investment.smart_investment_platform.user.web;

import com.speedster.investment.smart_investment_platform.shared.AbstractIntegrationTest;
import com.speedster.investment.smart_investment_platform.user.application.dto.AuthResponse;
import com.speedster.investment.smart_investment_platform.user.application.dto.LoginRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.RegisterRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.UserResponse;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import com.speedster.investment.smart_investment_platform.user.infrastructure.persistance.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDataBase(){
        ((JpaUserRepository) userRepository).deleteAll();
    }

    @Test
    @DisplayName("Should register user and return 201")
    void shouldRegisterUserAndReturn201() {
        // given
        RegisterRequest request = new RegisterRequest(
                "jan@example.com",
                "password123",
                "Jan",
                "Kowalski"
        );

        // when
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                UserResponse.class
        );

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("jan@example.com");
    }

    @Test
    @DisplayName("Should return 409 when email already registered")
    void shouldReturn409WhenEmailAlreadyRegistered() {
        User existingUser = User.builder()
                .email("jan@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(
                "jan@example.com",   // ← ten sam email
                "password123",
                "Anna",
                "Nowak"
        );

        // when
        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                Object.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should login and return JWT token")
    void shouldLoginAndReturnJwtToken() {
        // given
        User user = User.builder()
                .email("jan@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest(
                "jan@example.com",
                "password123"
        );

        // when
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                AuthResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token())
                .isNotBlank()
                .contains(".");
    }

    @Test
    @DisplayName("Should return 403 for protected endpoint without token")
    void shouldReturn403ForProtectedEndpointWithoutToken() {
        // when
        ResponseEntity<Object> response = restTemplate.getForEntity(
                "/api/v1/users/" + UUID.randomUUID(),
                Object.class
        );

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
