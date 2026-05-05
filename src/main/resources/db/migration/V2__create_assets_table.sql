-- V2__create_assets_table.sql
CREATE TABLE assets (
                        id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        user_id       UUID NOT NULL REFERENCES users(id),
                        name          VARCHAR(255) NOT NULL,
                        asset_type    VARCHAR(50)  NOT NULL,
                        quantity      DECIMAL(19,4) NOT NULL,
                        purchase_price DECIMAL(19,4) NOT NULL,
                        current_value  DECIMAL(19,4),
                        currency      VARCHAR(10)  NOT NULL DEFAULT 'PLN',
                        notes         TEXT,
                        created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
                        updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_assets_user_id ON assets(user_id);
CREATE INDEX idx_assets_asset_type ON assets(asset_type);