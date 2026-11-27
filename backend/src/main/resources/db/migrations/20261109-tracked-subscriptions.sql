CREATE TABLE IF NOT EXISTS tracked_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    amount BIGINT NOT NULL,
    billing_cycle VARCHAR(10) NOT NULL,
    billing_date DATE NOT NULL,
    active BOOLEAN NOT NULL,
    reminder_days INTEGER,
    receipt_id VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_tracked_subscription_member FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT uq_tracked_subscription_receipt UNIQUE (member_id, receipt_id),
    CONSTRAINT chk_tracked_subscription_amount CHECK (amount BETWEEN 1 AND 999999999),
    CONSTRAINT chk_tracked_subscription_cycle CHECK (billing_cycle IN ('MONTHLY', 'YEARLY')),
    CONSTRAINT chk_tracked_subscription_reminder CHECK (reminder_days BETWEEN 0 AND 30)
);
