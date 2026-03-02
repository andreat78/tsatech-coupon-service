CREATE TABLE coupon_service_coupon (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    discount_type VARCHAR(16) NOT NULL,
    value NUMERIC(15,4) NOT NULL,
    min_total NUMERIC(15,4) NOT NULL DEFAULT 0,
    max_discount NUMERIC(15,4),
    currency VARCHAR(3) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    date_start TIMESTAMPTZ,
    date_end TIMESTAMPTZ,
    usage_limit INT,
    used_count INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_coupon_service_coupon_code ON coupon_service_coupon(code);
CREATE INDEX idx_coupon_service_coupon_active ON coupon_service_coupon(active);
