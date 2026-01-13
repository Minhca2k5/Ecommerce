-- =========================================================
-- INDEXES (Performance Optimization)
-- =========================================================

-- Products
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_products_status_category_created ON products(status, category_id, created_at);

-- Product Images
CREATE INDEX idx_product_images_product_primary ON product_images(product_id, is_primary);

-- Orders
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at);
CREATE INDEX idx_orders_user_updated ON orders(user_id, updated_at);
CREATE INDEX idx_orders_status_created ON orders(status, created_at);

-- Order Items
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product_created ON order_items(product_id, created_at);
CREATE INDEX idx_order_items_order_updated ON order_items(order_id, updated_at);

-- Cart Items
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_cart_product ON cart_items(cart_id, product_id);
CREATE INDEX idx_cart_items_cart_updated ON cart_items(cart_id, updated_at);

-- Reviews
CREATE INDEX idx_reviews_product_created ON reviews(product_id, created_at);
CREATE INDEX idx_reviews_product_updated ON reviews(product_id, updated_at);
CREATE INDEX idx_reviews_user_updated ON reviews(user_id, updated_at);

-- Recent Views
CREATE INDEX idx_recent_views_user_created_at ON recent_views(user_id, created_at);
CREATE INDEX idx_recent_views_product_created ON recent_views(product_id, created_at);
CREATE INDEX idx_recent_views_user_updated ON recent_views(user_id, updated_at);

-- Wishlists
CREATE INDEX idx_wishlists_product_created ON wishlists(product_id, created_at);
CREATE INDEX idx_wishlists_user_updated ON wishlists(user_id, updated_at);

-- Search Logs
CREATE INDEX idx_search_logs_keyword ON search_logs(keyword);
CREATE INDEX idx_search_logs_created_at ON search_logs(created_at);
CREATE INDEX idx_search_logs_user ON search_logs(user_id);
CREATE INDEX idx_search_logs_user_updated ON search_logs(user_id, updated_at);

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
