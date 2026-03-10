CREATE TABLE IF NOT EXISTS coupon_service_coupon_translation (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    language_code VARCHAR(5) NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_coupon_service_coupon_translation_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupon_service_coupon(id) ON DELETE CASCADE,
    CONSTRAINT uk_coupon_translation UNIQUE (coupon_id, language_code)
);

CREATE INDEX IF NOT EXISTS idx_coupon_service_coupon_translation_coupon_id
    ON coupon_service_coupon_translation(coupon_id);

INSERT INTO coupon_service_coupon_translation (coupon_id, language_code, name)
SELECT c.id, l.language_code, c.name
FROM coupon_service_coupon c
CROSS JOIN (
    VALUES ('it'), ('en'), ('fr'), ('de'), ('es')
) AS l(language_code)
ON CONFLICT (coupon_id, language_code) DO NOTHING;
