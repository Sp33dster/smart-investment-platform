-- V4__create_notifications_table.sql
CREATE TABLE notifications (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id     UUID NOT NULL REFERENCES users(id),
                               asset_id    UUID NOT NULL REFERENCES assets(id),
                               title       VARCHAR(255) NOT NULL,
                               message     TEXT NOT NULL,
                               type        VARCHAR(50) NOT NULL,
                               read        BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read);