-- MySQL 8 / H2 MySQL mode. members, products는 상대 도메인이 먼저 생성해야 한다.
-- 최초 설치용 DDL. 기존 테이블이 있다면 별도 마이그레이션을 작성한다.
CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_subscription_member (member_id, product_id, end_date),
    INDEX idx_subscription_expiry (status, end_date),
    CONSTRAINT fk_subscription_member FOREIGN KEY(member_id) REFERENCES members(member_id),
    CONSTRAINT fk_subscription_product FOREIGN KEY(product_id) REFERENCES products(product_id),
    CONSTRAINT ck_subscription_status CHECK(status IN ('ACTIVE','CANCELLED','EXPIRED')),
    CONSTRAINT ck_subscription_dates CHECK(end_date >= start_date)
);
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    request_key VARCHAR(64) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_payment_subscription FOREIGN KEY(subscription_id) REFERENCES subscriptions(subscription_id),
    CONSTRAINT fk_payment_member FOREIGN KEY(member_id) REFERENCES members(member_id),
    CONSTRAINT uk_payment_request UNIQUE(member_id,request_key),
    CONSTRAINT ck_payment_amount CHECK(amount > 0),
    CONSTRAINT ck_payment_method CHECK(payment_method IN ('CARD','BANK_TRANSFER')),
    CONSTRAINT ck_payment_status CHECK(status IN ('PENDING','COMPLETED','FAILED','REFUNDED'))
);
CREATE TABLE IF NOT EXISTS refunds (
    refund_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    total_days BIGINT NULL,
    remaining_days BIGINT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    refunded_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_refund_payment FOREIGN KEY(payment_id) REFERENCES payments(payment_id),
    CONSTRAINT uk_refund_payment UNIQUE(payment_id),
    CONSTRAINT ck_refund_amount CHECK(amount > 0),
    CONSTRAINT ck_refund_status CHECK(status IN ('REQUESTED','COMPLETED','REJECTED'))
);
