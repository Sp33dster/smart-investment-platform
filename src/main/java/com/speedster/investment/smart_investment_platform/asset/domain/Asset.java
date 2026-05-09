package com.speedster.investment.smart_investment_platform.asset.domain;

import com.speedster.investment.smart_investment_platform.shared.audit.AuditableEntity;
import com.speedster.investment.smart_investment_platform.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Asset extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "purchase_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal purchasePrice;

    @Column(name = "current_value", precision = 19, scale = 4)
    private BigDecimal currentValue;

    @Column(nullable = false)
    private String currency;

    @Column(name = "external_id")
    private String externalId;

    @Column
    private String notes;

    public BigDecimal calculateGainLoss() {
        if (currentValue == null) return BigDecimal.ZERO;
        return currentValue.subtract(purchasePrice.multiply(quantity));
    }

    public BigDecimal calculateGainLossPercent() {
        if (currentValue == null) return BigDecimal.ZERO;
        BigDecimal totalPurchase = purchasePrice.multiply(quantity);
        if (totalPurchase.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return currentValue.subtract(totalPurchase)
                .divide(totalPurchase, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
