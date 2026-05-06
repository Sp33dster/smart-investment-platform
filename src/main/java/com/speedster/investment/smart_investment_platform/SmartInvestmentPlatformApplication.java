package com.speedster.investment.smart_investment_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class SmartInvestmentPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartInvestmentPlatformApplication.class, args);
	}

}
