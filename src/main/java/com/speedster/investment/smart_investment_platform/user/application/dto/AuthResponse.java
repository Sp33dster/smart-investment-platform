package com.speedster.investment.smart_investment_platform.user.application.dto;

public record AuthResponse(
        String token,
        String email,
        String firstName,
        String role
) {}
