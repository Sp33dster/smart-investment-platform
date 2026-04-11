package com.speedster.investment.smart_investment_platform.user.application.service;

import com.speedster.investment.smart_investment_platform.shared.exception.BusinessException;
import com.speedster.investment.smart_investment_platform.shared.exception.ResourceNotFoundException;
import com.speedster.investment.smart_investment_platform.user.application.dto.AuthResponse;
import com.speedster.investment.smart_investment_platform.user.application.dto.LoginRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.RegisterRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.UserResponse;
import com.speedster.investment.smart_investment_platform.user.application.dto.mapper.UserMapper;
import com.speedster.investment.smart_investment_platform.user.domain.Role;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import com.speedster.investment.smart_investment_platform.user.domain.UserRepository;
import com.speedster.investment.smart_investment_platform.user.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;          // ← z PasswordConfig
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // ← z ApplicationConfig
    private final UserMapper userMapper;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    "Email already registered",
                    HttpStatus.CONFLICT
            );
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.USER)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", request.email()));

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getRole().name()
        );
    }
}
