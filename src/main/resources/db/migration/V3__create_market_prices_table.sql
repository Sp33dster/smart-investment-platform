-- V3__create_market_prices_table.sql

CREATE TABLE market_prices (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               symbol      VARCHAR(50)   NOT NULL,
                               price       DECIMAL(19,4) NOT NULL,
                               currency    VARCHAR(10)   NOT NULL,
                               source      VARCHAR(100)  NOT NULL,
                               fetched_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_market_prices_symbol ON market_prices(symbol);
CREATE INDEX idx_market_prices_fetched_at ON market_prices(fetched_at);