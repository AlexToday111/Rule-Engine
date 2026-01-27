CREATE TABLE rules (
    id BIGSERIAL PRIMARY KEY,
    decision_type VARCHAR(100) NOT NULL,
    priority INT NOT NULL ,
    enabled BOOLEAN NOT NULL,
    condition TEXT NOT NULL ,
    effect TEXT NOT NULL ,
    description VARCHAR(255) NOT NULL
);

CREATE INDEX idx_rules_decision_enabled_priority
    ON rules (decision_type, enabled, priority);