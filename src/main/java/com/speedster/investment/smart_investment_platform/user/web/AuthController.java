package com.speedster.investment.smart_investment_platform.user.web;

import com.speedster.investment.smart_investment_platform.user.application.dto.AuthResponse;
import com.speedster.investment.smart_investment_platform.user.application.dto.LoginRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.RegisterRequest;
import com.speedster.investment.smart_investment_platform.user.application.dto.UserResponse;
import com.speedster.investment.smart_investment_platform.user.application.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request){
        return authService.login(request);
    }
}
