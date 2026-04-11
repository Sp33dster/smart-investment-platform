package com.speedster.investment.smart_investment_platform.user.application;

import com.speedster.investment.smart_investment_platform.shared.exception.BusinessException;
import com.speedster.investment.smart_investment_platform.user.application.dto.AuthResponse;
import com.speedster.investment.smart_investment_platform.user.application.dto.LoginRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.RegisterRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.UserResponse;
import com.speedster.investment.smart_investment_platform.user.application.dto.mapper.UserMapper;
import com.speedster.investment.smart_investment_platform.user.application.service.AuthService;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import com.speedster.investment.smart_investment_platform.user.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "jan@example.com",
                "password123",
                "Jan",
                "Kowalski"
        );

        savedUser = User.builder()
                .email("jan@example.com")
                .password("hashedPassword")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // given
        given(userRepository.existsByEmail("jan@example.com"))
                .willReturn(false);
        given(passwordEncoder.encode("password123"))
                .willReturn("hashedPassword");
        given(userRepository.save(any(User.class)))
                .willReturn(savedUser);
        given(userMapper.toResponse(savedUser))
                .willReturn(new UserResponse(
                        UUID.randomUUID(),
                        "jan@example.com",
                        "Jan",
                        "Kowalski",
                        "USER",
                        Instant.now()
                ));

        // when
        UserResponse response = authService.register(registerRequest);

        // then
        assertThat(response.email()).isEqualTo("jan@example.com");
        assertThat(response.firstName()).isEqualTo("Jan");

        then(userRepository).should(times(1)).save(any(User.class));

        then(passwordEncoder).should(times(1)).encode("password123");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // given
        given(userRepository.existsByEmail("jan@example.com"))
                .willReturn(true);  // ← email już zajęty

        // when & then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already registered");

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should never save plain text password")
    void shouldNeverSavePlainTextPassword() {
        // given
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("hashedPassword");
        given(userRepository.save(any())).willReturn(savedUser);
        given(userMapper.toResponse(any())).willReturn(mock(UserResponse.class));

        // when
        authService.register(registerRequest);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPassword())
                .isNotEqualTo("password123")
                .isEqualTo("hashedPassword");
    }


    @Test
    @DisplayName("Should return token on successful login")
    void shouldReturnTokenOnSuccessfulLogin() {
        // given
        LoginRequest loginRequest = new LoginRequest(
                "jan@example.com",
                "password123"
        );

        given(userRepository.findByEmail("jan@example.com"))
                .willReturn(Optional.of(savedUser));
        given(jwtService.generateToken(savedUser))
                .willReturn("mocked.jwt.token");

        // when
        AuthResponse response = authService.login(loginRequest);

        // then
        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        assertThat(response.email()).isEqualTo("jan@example.com");
    }
}