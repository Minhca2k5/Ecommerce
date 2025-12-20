-- =========================================================
-- 0. DATABASE SETUP
-- =========================================================
CREATE DATABASE IF NOT EXISTS ecommerce
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ecommerce;

-- =========================================================
-- 1. USERS / ROLES / USER_ROLES
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    username     VARCHAR(64) NOT NULL UNIQUE,
    email        VARCHAR(128) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(128),
    phone        VARCHAR(32),
    enabled      BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    name         VARCHAR(32) NOT NULL UNIQUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id      BIGINT NOT NULL,
    role_id      BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 2. ADDRESSES
-- =========================================================
CREATE TABLE IF NOT EXISTS addresses (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    line1        VARCHAR(255) NOT NULL,
    line2        VARCHAR(255),
    city         VARCHAR(128),
    state        VARCHAR(128),
    country      VARCHAR(64),
    zipcode      VARCHAR(32),
    is_default   BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_addr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 3. CATEGORIES
-- =========================================================
CREATE TABLE IF NOT EXISTS categories (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    name         VARCHAR(128) NOT NULL,
    slug         VARCHAR(128) NOT NULL UNIQUE,
    parent_id    BIGINT,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 4. PRODUCTS
-- =========================================================
CREATE TABLE IF NOT EXISTS products (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    category_id  BIGINT,
    name         VARCHAR(160) NOT NULL,
    slug         VARCHAR(160) NOT NULL UNIQUE,
    sku          VARCHAR(64) NOT NULL UNIQUE,
    description  TEXT,
    price        DECIMAL(12,2) NOT NULL,
    currency     VARCHAR(8) DEFAULT 'VND',
    status       VARCHAR(16) DEFAULT 'ACTIVE',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_prod_cat FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE','DRAFT','ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 5. PRODUCT_IMAGES
-- =========================================================
CREATE TABLE IF NOT EXISTS product_images (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    product_id   BIGINT NOT NULL,
    url          VARCHAR(512) NOT NULL,
    is_primary   BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_pimg_prod FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 6. CARTS & CART_ITEMS
-- =========================================================
CREATE TABLE IF NOT EXISTS carts (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL UNIQUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    cart_id             BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    unit_price_snapshot DECIMAL(12,2) NOT NULL,
    quantity            INT DEFAULT 1,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_citem_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_citem_prod FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT ck_cart_items_qty_pos CHECK (quantity >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 7. ORDERS & ORDER_ITEMS & PAYMENTS
-- =========================================================
CREATE TABLE IF NOT EXISTS orders (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    user_id             BIGINT NOT NULL,
    address_id_snapshot BIGINT,
    total_amount        DECIMAL(12,2) NOT NULL,
    voucher_id          BIGINT,
    discount_amount     DECIMAL(12,2) DEFAULT 0,
    currency            VARCHAR(8) DEFAULT 'VND',
    status              VARCHAR(16) DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING','PAID','CANCELLED','SHIPPED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
    id                    BIGINT NOT NULL AUTO_INCREMENT,
    order_id              BIGINT NOT NULL,
    product_id            BIGINT NOT NULL,
    product_name_snapshot VARCHAR(160),
    unit_price_snapshot   DECIMAL(12,2) NOT NULL,
    quantity              INT DEFAULT 1,
    line_total            DECIMAL(12,2),
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_oitm_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_oitm_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    order_id        BIGINT NOT NULL UNIQUE,
    method          VARCHAR(16) DEFAULT 'COD',
    amount          DECIMAL(12,2) NOT NULL,
    status          VARCHAR(16) DEFAULT 'INITIATED',
    provider_txn_id VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT ck_payments_status CHECK (status IN ('INITIATED','SUCCEEDED','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 8. REVIEWS
-- =========================================================
CREATE TABLE IF NOT EXISTS reviews (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    rating       INT NOT NULL,
    comment      TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_rev_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_rev_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 9. WAREHOUSES & INVENTORY
-- =========================================================
CREATE TABLE IF NOT EXISTS warehouses (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    code         VARCHAR(50) NOT NULL UNIQUE,
    name         VARCHAR(200) NOT NULL,
    address      VARCHAR(255),
    city         VARCHAR(100),
    state        VARCHAR(100),
    country      VARCHAR(100),
    zipcode      VARCHAR(50),
    phone        VARCHAR(50),
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    product_id   BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    stock_qty    INT DEFAULT 0 NOT NULL,
    reserved_qty INT DEFAULT 0 NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_inv_prod FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
    CONSTRAINT uq_inv_prod_wh UNIQUE (product_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 10. WISHLISTS
-- =========================================================
CREATE TABLE IF NOT EXISTS wishlists (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 11. RECENT_VIEWS
-- =========================================================
CREATE TABLE IF NOT EXISTS recent_views (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_recent_view_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recent_view_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_recent_view_user_product UNIQUE (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 12. SEARCH_LOGS
-- =========================================================
CREATE TABLE IF NOT EXISTS search_logs (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT,
    keyword      VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_search_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 13. VOUCHERS & VOUCHER_USES
-- =========================================================
CREATE TABLE IF NOT EXISTS vouchers (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    code                VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    discount_type       VARCHAR(20) NOT NULL,
    discount_value      DECIMAL(12,2),
    max_discount_amount DECIMAL(12,2),
    min_order_total     DECIMAL(12,2),
    start_at            TIMESTAMP NOT NULL,
    end_at              TIMESTAMP NOT NULL,
    usage_limit_global  INT,
    usage_limit_user    INT,
    status              VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT ck_vouchers_discount_type CHECK (discount_type IN ('PERCENT','FIXED','FREESHIP')),
    CONSTRAINT ck_vouchers_status CHECK (status IN ('ACTIVE','INACTIVE','EXPIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS voucher_uses (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    voucher_id      BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_voucher_uses_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_uses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_uses_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE orders
  ADD CONSTRAINT fk_orders_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL;

-- =========================================================
-- 14. BANNERS
-- =========================================================
CREATE TABLE IF NOT EXISTS banners (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    title        VARCHAR(255) NOT NULL,
    image_url    VARCHAR(512) NOT NULL,
    target_url   VARCHAR(512),
    position     INT DEFAULT 0 NOT NULL,
    is_active    BOOLEAN DEFAULT TRUE NOT NULL,
    start_at     TIMESTAMP,
    end_at       TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 15. REFRESH_TOKENS
-- =========================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    token        VARCHAR(255) NOT NULL UNIQUE,
    expiry_date  DATETIME NOT NULL,
    user_id      BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- =========================================================
-- 16. INDEXES (Performance Optimization)
-- =========================================================
-- Products
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_price ON products(price);

-- Orders
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at);

-- Order Items
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- Cart Items
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- Reviews
CREATE INDEX idx_reviews_product_created ON reviews(product_id, created_at);

-- Recent Views
CREATE INDEX idx_recent_views_user_created_at ON recent_views(user_id, created_at);

-- Search Logs
CREATE INDEX idx_search_logs_keyword ON search_logs(keyword);
CREATE INDEX idx_search_logs_created_at ON search_logs(created_at);
CREATE INDEX idx_search_logs_user ON search_logs(user_id);

-- Voucher Uses
CREATE INDEX idx_voucher_uses_voucher ON voucher_uses(voucher_id);
CREATE INDEX idx_voucher_uses_user ON voucher_uses(user_id);
CREATE INDEX idx_voucher_uses_order ON voucher_uses(order_id);

-- Banners
CREATE INDEX idx_banners_active_position ON banners(is_active, position);
CREATE INDEX idx_banners_time_range ON banners(start_at, end_at);

-- Notifications
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);

-- Refresh Tokens
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
