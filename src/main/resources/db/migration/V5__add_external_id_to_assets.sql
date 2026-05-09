-- V5__add_external_id_to_assets.sql
ALTER TABLE assets
    ADD COLUMN external_id VARCHAR(50);