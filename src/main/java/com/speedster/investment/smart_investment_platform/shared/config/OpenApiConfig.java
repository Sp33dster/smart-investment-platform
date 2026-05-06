package com.speedster.investment.smart_investment_platform.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Investment Platform API")
                        .description("Investment portfolio tracker")
                        .version("v1.0")
                        .contact(new Contact()
                        .name("Bartłomiej Gajewski")
                        .email("bartlomiejgajewski90@gmail.com")
                        .url("https://github.com/twoj-profil")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqYW5AZXhhbXBsZS5jb20iLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc3ODA3NzM5NywiZXhwIjoxNzc4MTYzNzk3fQ.8PHyzAk-APxBBFPGLHRzxoC64sm0n00rHk-hFpqs3DEaaBNnL0ec811Fzw27Lgff")));
    }
}
