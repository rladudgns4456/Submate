-- 2026-09-30
-- Member 인증 및 Product/Category 기능용 DB 변경
-- 담당: 김현빈

-- members 테이블 확장
ALTER TABLE members
    MODIFY COLUMN member_id BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE members
    ADD COLUMN password VARCHAR(255) NOT NULL AFTER email;

ALTER TABLE members
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER role;

-- categories 테이블 생성
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- products 테이블 생성
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
);