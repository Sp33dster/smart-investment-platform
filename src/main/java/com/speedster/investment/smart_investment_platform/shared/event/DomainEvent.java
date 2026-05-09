package com.speedster.investment.smart_investment_platform.shared.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
