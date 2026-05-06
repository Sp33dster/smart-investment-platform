package com.speedster.investment.smart_investment_platform.market.web;

import com.speedster.investment.smart_investment_platform.market.application.MarketService;
import com.speedster.investment.smart_investment_platform.market.application.dto.MarketPriceResponse;
import com.speedster.investment.smart_investment_platform.market.domain.MarketPrice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
@Tag(name = "Market", description = "Market prices endpoints")
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/price/{symbol}")
    @Operation(summary = "Get current price for symbol (e.g. XAU for gold)")
    public ResponseEntity<MarketPriceResponse> getCurrentPrice(@PathVariable String symbol){
        return marketService.getCurrentPrice(symbol)
                .map(price -> ResponseEntity.ok(toResponse(price)))
                .orElse(ResponseEntity.notFound().build());
    }

    private MarketPriceResponse toResponse(MarketPrice price){
        return new MarketPriceResponse(
                price.getSymbol(),
                price.getPrice(),
                price.getCurrency(),
                price.getSource(),
                price.getFetchedAt()
        );
    }
}
