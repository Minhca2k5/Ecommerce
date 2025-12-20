-- =========================================================
-- SEED DATA
-- =========================================================

-- 1. ROLES
INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');
INSERT INTO roles (id, name) VALUES (3, 'ROLE_MANAGER');

-- 2. USERS
-- Password hash for 'admin123' (example)
INSERT INTO users (id, username, email, password, full_name, phone, enabled) VALUES 
(1, 'admin', 'admin@example.com', '$2a$12$dyBIlz6Tt27ZRIbdR7gCjuF04SWyewzyOowrJW15cyTZ8/KbUncEa', 'System Administrator', '0123456789', TRUE),
(2, 'john', 'john@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'John Doe', '0988123123', TRUE),
(3, 'alice', 'alice@example.com', '$2a$12$duF.MI2l92NmBQDkn3pZkuCkLEWTye0J8W8btgjZ9jUgrNUneMEcC', 'Alice Nguyen', '0988456789', TRUE),
(4, 'bob', 'bob@example.com', '$2a$12$58YbJR6laEZrLavBrtbLbeZbjPSQPV5LXeubcIJT6QTshags0pbPu', 'Bob Tran', '0912345678', TRUE),
(5, 'charlie', 'charlie@example.com', '$2a$12$kKBCwJgL4VmmnQ5aK5M8Kuo9UxGlgbBztI2I83g0ZQvR4wQHwi2aq', 'Charlie Pham', '0933666888', TRUE),
(6, 'david', 'david@example.com', '$2a$12$WbR5mX9bpcrLfAMzx5LnqeI8eJfJVf1iR1njZu6wUpks/H4I8P97i', 'David Le', '0977555444', TRUE),
(7, 'emma', 'emma@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Emma Nguyen', '0900000007', TRUE),
(8, 'noah', 'noah@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Noah Tran', '0900000008', TRUE),
(9, 'olivia', 'olivia@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Olivia Pham', '0900000009', TRUE),
(10, 'liam', 'liam@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Liam Le', '0900000010', TRUE),
(11, 'ava', 'ava@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Ava Vo', '0900000011', TRUE),
(12, 'mason', 'mason@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Mason Do', '0900000012', TRUE),
(13, 'sophia', 'sophia@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Sophia Bui', '0900000013', TRUE),
(14, 'james', 'james@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'James Vu', '0900000014', TRUE),
(15, 'mia', 'mia@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Mia Hoang', '0900000015', TRUE),
(16, 'lucas', 'lucas@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Lucas Nguyen', '0900000016', TRUE),
(17, 'amelia', 'amelia@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Amelia Tran', '0900000017', TRUE),
(18, 'ethan', 'ethan@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Ethan Pham', '0900000018', TRUE),
(19, 'isabella', 'isabella@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Isabella Le', '0900000019', TRUE),
(20, 'benjamin', 'benjamin@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Benjamin Vo', '0900000020', TRUE),
(21, 'harper', 'harper@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Harper Do', '0900000021', TRUE),
(22, 'henry', 'henry@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Henry Bui', '0900000022', TRUE),
(23, 'evelyn', 'evelyn@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Evelyn Vu', '0900000023', TRUE),
(24, 'jackson', 'jackson@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Jackson Hoang', '0900000024', TRUE),
(25, 'sofia', 'sofia@example.com', '$2a$12$2Bx7Dn0ayHV3yRUI4ALpYuDDfFJ5xt7dhXAinZMCjtqQ9Rtln7Slm', 'Sofia Nguyen', '0900000025', TRUE);

-- 3. USER_ROLES
-- Admin has ROLE_ADMIN (2) and ROLE_USER (1)
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- Others have ROLE_USER (1)
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (3, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (4, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (5, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (6, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (7, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (8, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (9, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (10, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (11, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (12, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (13, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (14, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (15, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (16, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (17, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (18, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (19, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (20, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (21, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (22, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (23, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (24, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (25, 1);

-- 4. CATEGORIES
-- Root categories
INSERT INTO categories (id, name, slug, parent_id) VALUES (1, 'Electronics', 'electronics', NULL);
INSERT INTO categories (id, name, slug, parent_id) VALUES (2, 'Books', 'books', NULL);
INSERT INTO categories (id, name, slug, parent_id) VALUES (3, 'Fashion', 'fashion', NULL);

-- Child categories
INSERT INTO categories (id, name, slug, parent_id) VALUES (4, 'Laptops', 'laptops', 1);
INSERT INTO categories (id, name, slug, parent_id) VALUES (5, 'Smartphones', 'smartphones', 1);
INSERT INTO categories (id, name, slug, parent_id) VALUES (6, 'Men Clothing', 'men-clothing', 3);
INSERT INTO categories (id, name, slug, parent_id) VALUES (7, 'Women Clothing', 'women-clothing', 3);
INSERT INTO categories (id, name, slug, parent_id) VALUES (8, 'Programming Books', 'programming-books', 2);
INSERT INTO categories (id, name, slug, parent_id) VALUES (9, 'Tablets', 'tablets', 1);
INSERT INTO categories (id, name, slug, parent_id) VALUES (10, 'Audio', 'audio', 1);
INSERT INTO categories (id, name, slug, parent_id) VALUES (11, 'Cameras', 'cameras', 1);
INSERT INTO categories (id, name, slug, parent_id) VALUES (12, 'Accessories', 'accessories', 1);
INSERT INTO categories (id, name, slug, parent_id) VALUES (13, 'Shoes', 'shoes', 3);
INSERT INTO categories (id, name, slug, parent_id) VALUES (14, 'Skincare', 'skincare', 3);
INSERT INTO categories (id, name, slug, parent_id) VALUES (15, 'Fiction', 'fiction', 2);

-- 5. PRODUCTS
-- Smartphones
INSERT INTO products (id, category_id, name, slug, sku, description, price, currency, status) VALUES 
(1, 5, 'Smartphone X20', 'smartphone-x20', 'SMX20', '6.5-inch display, 128GB storage, triple camera, and fast charging.', 9990000, 'VND', 'ACTIVE'),
(2, 5, 'Smartphone A15', 'smartphone-a15', 'SMA15', 'Budget smartphone with 64GB storage and dual camera.', 3990000, 'VND', 'ACTIVE');

-- Laptops
INSERT INTO products (id, category_id, name, slug, sku, description, price, currency, status) VALUES 
(3, 4, 'Laptop Pro 14"', 'laptop-pro-14', 'LP14', 'Powerful 14-inch laptop with Intel i7 and 16GB RAM.', 19990000, 'VND', 'ACTIVE'),
(4, 4, 'Laptop Air 13"', 'laptop-air-13', 'LA13', 'Lightweight 13-inch laptop, 8GB RAM, 256GB SSD.', 14990000, 'VND', 'ACTIVE');

-- Accessories (Electronics root)
INSERT INTO products (id, category_id, name, slug, sku, description, price, currency, status) VALUES 
(5, 1, 'Wireless Headphones', 'wireless-headphones', 'WH-001', 'Noise-cancelling Bluetooth wireless headphones with 30-hour battery life.', 1299000, 'VND', 'ACTIVE'),
(6, 1, 'Power Bank 20000mAh', 'power-bank-20000', 'PB20000', 'High capacity power bank with fast charging support.', 590000, 'VND', 'ACTIVE');

-- Books
INSERT INTO products (id, category_id, name, slug, sku, description, price, currency, status) VALUES 
(7, 8, 'Clean Code', 'clean-code', 'BOOK-001', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin.', 450000, 'VND', 'ACTIVE'),
(8, 8, 'Design Patterns', 'design-patterns', 'BOOK-002', 'Elements of Reusable Object-Oriented Software.', 550000, 'VND', 'ACTIVE');

-- Fashion
INSERT INTO products (id, category_id, name, slug, sku, description, price, currency, status) VALUES 
(9, 6, 'Men T-Shirt', 'men-tshirt', 'TSHIRT-M01', 'Cotton t-shirt for men.', 250000, 'VND', 'ACTIVE'),
(10, 7, 'Women Dress', 'women-dress', 'WD-01', 'Elegant summer dress.', 390000, 'VND', 'ACTIVE');

-- More products (11-20)
INSERT INTO products (id, category_id, name, slug, sku, description, price, currency, status) VALUES
(11, 9, 'Tablet Z10', 'tablet-z10', 'TBZ10', '10.4-inch tablet, 128GB storage, quad speakers, long battery life.', 7490000, 'VND', 'ACTIVE'),
(12, 10, 'Bluetooth Speaker Mini', 'bluetooth-speaker-mini', 'SPK-MINI', 'Compact speaker, deep bass, 12-hour playtime, IPX7 water resistance.', 690000, 'VND', 'ACTIVE'),
(13, 12, 'USB-C Fast Charger 33W', 'usb-c-fast-charger-33w', 'CHG-33W', 'Fast charger with USB-C PD and PPS support, compact travel design.', 190000, 'VND', 'ACTIVE'),
(14, 11, 'Mirrorless Camera M50', 'mirrorless-camera-m50', 'CAM-M50', '24MP mirrorless camera, 4K video, flip screen, beginner friendly.', 12990000, 'VND', 'ACTIVE'),
(15, 8, 'Refactoring', 'refactoring', 'BOOK-003', 'Improving the Design of Existing Code by Martin Fowler.', 520000, 'VND', 'ACTIVE'),
(16, 15, 'The Silent City', 'the-silent-city', 'BOOK-004', 'A mystery fiction novel with unexpected twists and fast pacing.', 180000, 'VND', 'ACTIVE'),
(17, 13, 'Running Shoes Pro', 'running-shoes-pro', 'SHOE-RUN-01', 'Lightweight running shoes with responsive foam and breathable upper.', 1290000, 'VND', 'ACTIVE'),
(18, 14, 'Vitamin C Serum 30ml', 'vitamin-c-serum-30ml', 'SKIN-VC-30', 'Brightening serum with vitamin C, niacinamide, and hyaluronic acid.', 320000, 'VND', 'ACTIVE'),
(19, 12, 'MagSafe Phone Case', 'magsafe-phone-case', 'CASE-MAG-01', 'Slim protective case with MagSafe compatible magnets and soft touch.', 290000, 'VND', 'ACTIVE'),
(20, 10, 'Studio Headphones X', 'studio-headphones-x', 'HP-STUDIO-X', 'Over-ear studio headphones, neutral sound signature, detachable cable.', 1590000, 'VND', 'ACTIVE');

-- Extra products (21-25)
INSERT INTO products (id, category_id, name, slug, sku, description, price, currency, status) VALUES
(21, 11, 'Action Camera Go', 'action-camera-go', 'CAM-GO', '4K action camera with stabilization, waterproof case, and wide-angle lens.', 2490000, 'VND', 'ACTIVE'),
(22, 9, 'Tablet Pro 11\"', 'tablet-pro-11', 'TAB-PRO-11', '11-inch tablet, 256GB storage, high refresh display, and stylus support.', 10990000, 'VND', 'ACTIVE'),
(23, 13, 'Sneakers Street', 'sneakers-street', 'SHOES-STR-01', 'Everyday sneakers with durable outsole and breathable canvas upper.', 690000, 'VND', 'ACTIVE'),
(24, 6, 'Hoodie Classic', 'hoodie-classic', 'MC-HOODIE-01', 'Soft fleece hoodie, relaxed fit, great for daily wear and layering.', 490000, 'VND', 'ACTIVE'),
(25, 15, 'The Lost Planet', 'the-lost-planet', 'FIC-LOST-01', 'Sci-fi adventure fiction with epic exploration and a fast-paced plot.', 210000, 'VND', 'ACTIVE');

-- 5.5 BANNERS (public storefront)
INSERT INTO banners (id, title, image_url, target_url, position, is_active, start_at, end_at) VALUES
(1, 'Super sale Smartphone X20', 'https://picsum.photos/seed/banner-smartphone-x20/1400/600', '/products/slug/smartphone-x20', 1, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(2, 'Laptop Pro 14\" - Deal hot', 'https://picsum.photos/seed/banner-laptop-pro-14/1400/600', '/products/slug/laptop-pro-14', 2, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(3, 'Góc dev: Programming Books', 'https://picsum.photos/seed/banner-dev-books/1400/600', '/categories/slug/programming-books', 3, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(4, 'New drop: Tablet Z10', 'https://picsum.photos/seed/banner-tablet-z10/1400/600', '/products/slug/tablet-z10', 4, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(5, 'Audio week: Studio Headphones', 'https://picsum.photos/seed/banner-studio-headphones-x/1400/600', '/products/slug/studio-headphones-x', 5, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(6, 'Explore: Electronics', 'https://picsum.photos/seed/banner-electronics/1400/600', '/categories/slug/electronics', 6, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(7, 'Trending: Cameras', 'https://picsum.photos/seed/banner-cameras/1400/600', '/categories/slug/cameras', 7, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(8, 'Glow up: Skincare', 'https://picsum.photos/seed/banner-skincare/1400/600', '/categories/slug/skincare', 8, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(9, 'Fresh fits: Fashion', 'https://picsum.photos/seed/banner-fashion/1400/600', '/categories/slug/fashion', 9, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(10, 'Accessories you need', 'https://picsum.photos/seed/banner-accessories/1400/600', '/categories/slug/accessories', 10, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(11, 'Book picks: Fiction', 'https://picsum.photos/seed/banner-fiction/1400/600', '/categories/slug/fiction', 11, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(12, 'Audio deals', 'https://picsum.photos/seed/banner-audio/1400/600', '/categories/slug/audio', 12, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(13, 'Shoes week', 'https://picsum.photos/seed/banner-shoes/1400/600', '/categories/slug/shoes', 13, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(14, 'Hot: USB-C Chargers', 'https://picsum.photos/seed/banner-chargers/1400/600', '/products/slug/usb-c-fast-charger-33w', 14, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00'),
(15, 'New: Mirrorless M50', 'https://picsum.photos/seed/banner-m50/1400/600', '/products/slug/mirrorless-camera-m50', 15, TRUE, '2025-01-01 00:00:00', '2026-01-01 00:00:00');

-- 6. PRODUCT_IMAGES
INSERT INTO product_images (id, product_id, url, is_primary) VALUES 
(1, 1, 'https://picsum.photos/seed/smartphone-x20/900/900', TRUE),
(2, 3, 'https://picsum.photos/seed/laptop-pro-14/900/900', TRUE),
(3, 7, 'https://picsum.photos/seed/clean-code/900/900', TRUE),
(4, 9, 'https://picsum.photos/seed/men-tshirt/900/900', TRUE),
(5, 10, 'https://picsum.photos/seed/women-dress/900/900', TRUE),
(6, 2, 'https://picsum.photos/seed/smartphone-a15/900/900', TRUE),
(7, 4, 'https://picsum.photos/seed/laptop-air-13/900/900', TRUE),
(8, 5, 'https://picsum.photos/seed/wireless-headphones/900/900', TRUE),
(9, 6, 'https://picsum.photos/seed/power-bank-20000/900/900', TRUE),
(10, 8, 'https://picsum.photos/seed/design-patterns/900/900', TRUE),
(11, 11, 'https://picsum.photos/seed/tablet-z10/900/900', TRUE),
(12, 12, 'https://picsum.photos/seed/bluetooth-speaker-mini/900/900', TRUE),
(13, 13, 'https://picsum.photos/seed/usb-c-fast-charger-33w/900/900', TRUE),
(14, 14, 'https://picsum.photos/seed/mirrorless-camera-m50/900/900', TRUE),
(15, 15, 'https://picsum.photos/seed/refactoring/900/900', TRUE),
(16, 16, 'https://picsum.photos/seed/the-silent-city/900/900', TRUE),
(17, 17, 'https://picsum.photos/seed/running-shoes-pro/900/900', TRUE),
(18, 18, 'https://picsum.photos/seed/vitamin-c-serum-30ml/900/900', TRUE),
(19, 19, 'https://picsum.photos/seed/magsafe-phone-case/900/900', TRUE),
(20, 20, 'https://picsum.photos/seed/studio-headphones-x/900/900', TRUE);

INSERT INTO product_images (id, product_id, url, is_primary) VALUES
(21, 21, 'https://picsum.photos/seed/action-camera-go/900/900', TRUE),
(22, 22, 'https://picsum.photos/seed/tablet-pro-11/900/900', TRUE),
(23, 23, 'https://picsum.photos/seed/sneakers-street/900/900', TRUE),
(24, 24, 'https://picsum.photos/seed/hoodie-classic/900/900', TRUE),
(25, 25, 'https://picsum.photos/seed/the-lost-planet/900/900', TRUE);

-- 7. WAREHOUSES
INSERT INTO warehouses (id, code, name, address, city, country, is_active) VALUES
(4, 'HN02', 'Warehouse Hanoi 02', '12 Tay Ho', 'Ha Noi', 'Viet Nam', TRUE),
(5, 'HCM02', 'Warehouse HCM 02', '88 Vo Van Kiet', 'Ho Chi Minh', 'Viet Nam', TRUE),
(6, 'HP01', 'Warehouse Hai Phong 01', '19 Le Chan', 'Hai Phong', 'Viet Nam', TRUE),
(7, 'CT01', 'Warehouse Can Tho 01', '2 Ninh Kieu', 'Can Tho', 'Viet Nam', TRUE),
(8, 'BD01', 'Warehouse Binh Duong 01', '66 Thu Dau Mot', 'Binh Duong', 'Viet Nam', TRUE),
(9, 'NA01', 'Warehouse Nghe An 01', '7 Vinh', 'Vinh', 'Viet Nam', TRUE),
(10, 'KH01', 'Warehouse Khanh Hoa 01', '5 Nha Trang', 'Nha Trang', 'Viet Nam', TRUE),
(11, 'QN01', 'Warehouse Quang Ninh 01', '3 Ha Long', 'Ha Long', 'Viet Nam', TRUE),
(12, 'TH01', 'Warehouse Thanh Hoa 01', '9 Thanh Hoa', 'Thanh Hoa', 'Viet Nam', TRUE),
(13, 'GL01', 'Warehouse Gia Lai 01', '4 Pleiku', 'Pleiku', 'Viet Nam', TRUE),
(14, 'LA01', 'Warehouse Long An 01', '22 Tan An', 'Tan An', 'Viet Nam', TRUE),
(15, 'KG01', 'Warehouse Kien Giang 01', '6 Rach Gia', 'Rach Gia', 'Viet Nam', TRUE);

INSERT INTO warehouses (id, code, name, address, city, country, is_active) VALUES 
(1, 'HN01', 'Kho Hà Nội', '123 Hoàng Quốc Việt', 'Hà Nội', 'Việt Nam', TRUE),
(2, 'HCM01', 'Kho Hồ Chí Minh', '456 Nguyễn Văn Linh', 'Hồ Chí Minh', 'Việt Nam', TRUE),
(3, 'DN01', 'Kho Đà Nẵng', '789 Điện Biên Phủ', 'Đà Nẵng', 'Việt Nam', TRUE);

-- 8. INVENTORY
-- Smartphone X20
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (1, 1, 40, 5);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (1, 2, 30, 2);

-- Smartphone A15
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (2, 1, 60, 10);

-- Laptop Pro 14
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (3, 1, 25, 3);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (3, 3, 15, 0);

-- Laptop Air 13
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (4, 2, 20, 2);

-- Headphones & Powerbank
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (5, 2, 70, 10);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (6, 3, 80, 5);

-- More inventory (11-20)
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (11, 1, 35, 2);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (12, 2, 80, 6);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (13, 3, 120, 10);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (14, 1, 12, 1);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (15, 1, 60, 0);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (16, 1, 90, 0);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (17, 2, 55, 4);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (18, 2, 75, 5);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (19, 3, 100, 8);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (20, 2, 40, 3);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (21, 4, 55, 2);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (22, 5, 45, 1);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (23, 6, 95, 5);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (24, 7, 110, 7);
INSERT INTO inventory (product_id, warehouse_id, stock_qty, reserved_qty) VALUES (25, 8, 150, 9);

-- 9. ADDRESSES (25)
INSERT INTO addresses (id, user_id, line1, line2, city, state, country, zipcode, is_default) VALUES
(1, 1, '1 Tran Duy Hung', NULL, 'Ha Noi', 'Cau Giay', 'Viet Nam', '100000', TRUE),
(2, 2, '12 Kim Ma', NULL, 'Ha Noi', 'Ba Dinh', 'Viet Nam', '100000', TRUE),
(3, 3, '45 Nguyen Hue', NULL, 'Ho Chi Minh', 'District 1', 'Viet Nam', '700000', TRUE),
(4, 4, '56 Cau Dat', NULL, 'Hai Phong', 'Le Chan', 'Viet Nam', '180000', TRUE),
(5, 5, '101 Nguyen Trai', NULL, 'Can Tho', 'Ninh Kieu', 'Viet Nam', '900000', TRUE),
(6, 6, '23 Le Loi', NULL, 'Da Nang', 'Hai Chau', 'Viet Nam', '550000', TRUE),
(7, 7, '7 Thai Ha', NULL, 'Ha Noi', 'Dong Da', 'Viet Nam', '100000', TRUE),
(8, 8, '8 Vo Thi Sau', NULL, 'Ho Chi Minh', 'District 3', 'Viet Nam', '700000', TRUE),
(9, 9, '9 Hung Vuong', NULL, 'Hue', 'Thua Thien Hue', 'Viet Nam', '530000', TRUE),
(10, 10, '10 Tran Phu', NULL, 'Nha Trang', 'Khanh Hoa', 'Viet Nam', '650000', TRUE),
(11, 11, '11 Quang Trung', NULL, 'Vinh', 'Nghe An', 'Viet Nam', '430000', TRUE),
(12, 12, '12 Nguyen Van Cu', NULL, 'Binh Duong', 'Thu Dau Mot', 'Viet Nam', '750000', TRUE),
(13, 13, '13 Hang Bai', NULL, 'Ha Noi', 'Hoan Kiem', 'Viet Nam', '100000', TRUE),
(14, 14, '14 Nguyen Huu Tho', NULL, 'Ho Chi Minh', 'District 7', 'Viet Nam', '700000', TRUE),
(15, 15, '15 Vo Nguyen Giap', NULL, 'Da Nang', 'Son Tra', 'Viet Nam', '550000', TRUE),
(16, 16, '16 Me Tri', NULL, 'Ha Noi', 'Nam Tu Liem', 'Viet Nam', '100000', TRUE),
(17, 17, '17 Vo Van Ngan', NULL, 'Ho Chi Minh', 'Thu Duc', 'Viet Nam', '700000', TRUE),
(18, 18, '18 Lach Tray', NULL, 'Hai Phong', 'Hong Bang', 'Viet Nam', '180000', TRUE),
(19, 19, '19 30/4', NULL, 'Can Tho', 'Binh Thuy', 'Viet Nam', '900000', TRUE),
(20, 20, '20 Le Hong Phong', NULL, 'Nha Trang', 'Vinh Hai', 'Viet Nam', '650000', TRUE),
(21, 21, '21 Nguyen Sy Sach', NULL, 'Vinh', 'Cua Nam', 'Viet Nam', '430000', TRUE),
(22, 22, '22 DT743', NULL, 'Binh Duong', 'Di An', 'Viet Nam', '750000', TRUE),
(23, 23, '23 Nguyen Van Cu', NULL, 'Ha Noi', 'Long Bien', 'Viet Nam', '100000', TRUE),
(24, 24, '24 Dien Bien Phu', NULL, 'Ho Chi Minh', 'Binh Thanh', 'Viet Nam', '700000', TRUE),
(25, 25, '25 Nguyen Van Linh', NULL, 'Da Nang', 'Thanh Khe', 'Viet Nam', '550000', TRUE);

-- 10. CARTS (25) + CART_ITEMS (40)
INSERT INTO carts (id, user_id) VALUES
(1,1),(2,2),(3,3),(4,4),(5,5),(6,6),(7,7),(8,8),(9,9),(10,10),
(11,11),(12,12),(13,13),(14,14),(15,15),(16,16),(17,17),(18,18),(19,19),(20,20),
(21,21),(22,22),(23,23),(24,24),(25,25);

INSERT INTO cart_items (id, cart_id, product_id, unit_price_snapshot, quantity) VALUES
(1, 1, 1, 9990000, 1),
(2, 1, 7, 450000, 1),
(3, 2, 2, 3990000, 1),
(4, 2, 12, 690000, 2),
(5, 3, 3, 19990000, 1),
(6, 3, 13, 190000, 1),
(7, 4, 4, 14990000, 1),
(8, 4, 6, 590000, 2),
(9, 5, 5, 1299000, 1),
(10, 5, 11, 7490000, 1),
(11, 6, 8, 550000, 1),
(12, 6, 14, 12990000, 1),
(13, 7, 9, 250000, 2),
(14, 7, 15, 520000, 1),
(15, 8, 10, 390000, 1),
(16, 8, 16, 180000, 1),
(17, 9, 17, 1290000, 1),
(18, 9, 19, 290000, 2),
(19, 10, 18, 320000, 1),
(20, 10, 20, 1590000, 1),
(21, 11, 21, 2490000, 1),
(22, 11, 7, 450000, 1),
(23, 12, 22, 10990000, 1),
(24, 12, 13, 190000, 2),
(25, 13, 23, 690000, 1),
(26, 13, 5, 1299000, 1),
(27, 14, 24, 490000, 2),
(28, 14, 6, 590000, 1),
(29, 15, 25, 210000, 1),
(30, 15, 12, 690000, 1),
(31, 16, 1, 9990000, 1),
(32, 16, 19, 290000, 1),
(33, 17, 2, 3990000, 1),
(34, 17, 14, 12990000, 1),
(35, 18, 3, 19990000, 1),
(36, 18, 11, 7490000, 1),
(37, 19, 4, 14990000, 1),
(38, 19, 13, 190000, 2),
(39, 20, 5, 1299000, 1),
(40, 20, 18, 320000, 1);

-- 11. ORDERS (25) + ORDER_ITEMS (27) + PAYMENTS (25)
INSERT INTO orders (id, user_id, address_id_snapshot, total_amount, voucher_id, discount_amount, currency, status) VALUES
(1, 2, 2, 10890000, 1, 200000, 'VND', 'PAID'),
(2, 3, 3, 390000, NULL, 0, 'VND', 'PENDING'),
(3, 4, 4, 20740000, 2, 500000, 'VND', 'SHIPPED'),
(4, 5, 5, 450000, NULL, 0, 'VND', 'CANCELLED'),
(5, 6, 6, 3990000, 3, 150000, 'VND', 'PENDING'),
(6, 7, 7, 1299000, NULL, 0, 'VND', 'PAID'),
(7, 8, 8, 14990000, 4, 300000, 'VND', 'PAID'),
(8, 9, 9, 690000, NULL, 0, 'VND', 'SHIPPED'),
(9, 10, 10, 450000, NULL, 0, 'VND', 'PAID'),
(10, 11, 11, 590000, 2, 80000, 'VND', 'PENDING'),
(11, 12, 12, 520000, NULL, 0, 'VND', 'PAID'),
(12, 13, 13, 7490000, 1, 250000, 'VND', 'PAID'),
(13, 14, 14, 1290000, NULL, 0, 'VND', 'CANCELLED'),
(14, 15, 15, 180000, NULL, 0, 'VND', 'PAID'),
(15, 16, 16, 1590000, 3, 100000, 'VND', 'SHIPPED'),
(16, 17, 17, 320000, NULL, 0, 'VND', 'PAID'),
(17, 18, 18, 12990000, 4, 500000, 'VND', 'PENDING'),
(18, 19, 19, 690000, NULL, 0, 'VND', 'PAID'),
(19, 20, 20, 2490000, NULL, 0, 'VND', 'PAID'),
(20, 21, 21, 10990000, 2, 200000, 'VND', 'PENDING'),
(21, 22, 22, 490000, NULL, 0, 'VND', 'PAID'),
(22, 23, 23, 210000, NULL, 0, 'VND', 'PAID'),
(23, 24, 24, 690000, NULL, 0, 'VND', 'SHIPPED'),
(24, 25, 25, 2490000, 1, 150000, 'VND', 'PAID'),
(25, 2, 2, 390000, NULL, 0, 'VND', 'PENDING');

INSERT INTO order_items (id, order_id, product_id, product_name_snapshot, unit_price_snapshot, quantity, line_total) VALUES
(1, 1, 1, 'Smartphone X20', 9990000, 1, 9990000),
(2, 1, 7, 'Clean Code', 450000, 2, 900000),
(3, 2, 10, 'Women Dress', 390000, 1, 390000),
(4, 3, 3, 'Laptop Pro 14\"', 19990000, 1, 19990000),
(5, 3, 9, 'Men T-Shirt', 250000, 3, 750000),
(6, 4, 7, 'Clean Code', 450000, 1, 450000),
(7, 5, 2, 'Smartphone A15', 3990000, 1, 3990000),
(8, 6, 5, 'Wireless Headphones', 1299000, 1, 1299000),
(9, 7, 4, 'Laptop Air 13\"', 14990000, 1, 14990000),
(10, 8, 12, 'Bluetooth Speaker Mini', 690000, 1, 690000),
(11, 9, 7, 'Clean Code', 450000, 1, 450000),
(12, 10, 6, 'Power Bank 20000mAh', 590000, 1, 590000),
(13, 11, 15, 'Refactoring', 520000, 1, 520000),
(14, 12, 11, 'Tablet Z10', 7490000, 1, 7490000),
(15, 13, 17, 'Running Shoes Pro', 1290000, 1, 1290000),
(16, 14, 16, 'The Silent City', 180000, 1, 180000),
(17, 15, 20, 'Studio Headphones X', 1590000, 1, 1590000),
(18, 16, 18, 'Vitamin C Serum 30ml', 320000, 1, 320000),
(19, 17, 14, 'Mirrorless Camera M50', 12990000, 1, 12990000),
(20, 18, 23, 'Sneakers Street', 690000, 1, 690000),
(21, 19, 21, 'Action Camera Go', 2490000, 1, 2490000),
(22, 20, 22, 'Tablet Pro 11\"', 10990000, 1, 10990000),
(23, 21, 24, 'Hoodie Classic', 490000, 1, 490000),
(24, 22, 25, 'The Lost Planet', 210000, 1, 210000),
(25, 23, 19, 'MagSafe Phone Case', 290000, 1, 290000),
(26, 24, 8, 'Design Patterns', 550000, 1, 550000),
(27, 25, 10, 'Women Dress', 390000, 1, 390000);

INSERT INTO payments (id, order_id, method, amount, status, provider_txn_id) VALUES
(1, 1, 'VNPAY', 10890000, 'SUCCEEDED', 'TXN123456'),
(2, 2, 'COD', 390000, 'INITIATED', NULL),
(3, 3, 'PAYPAL', 20740000, 'SUCCEEDED', 'PAYPAL789'),
(4, 4, 'COD', 450000, 'INITIATED', NULL),
(5, 5, 'VNPAY', 3990000, 'SUCCEEDED', 'TXN-0005'),
(6, 6, 'PAYPAL', 1299000, 'SUCCEEDED', 'PAY-0006'),
(7, 7, 'VNPAY', 14990000, 'SUCCEEDED', 'TXN-0007'),
(8, 8, 'COD', 690000, 'SUCCEEDED', NULL),
(9, 9, 'VNPAY', 450000, 'SUCCEEDED', 'TXN-0009'),
(10, 10, 'COD', 590000, 'INITIATED', NULL),
(11, 11, 'PAYPAL', 520000, 'SUCCEEDED', 'PAY-0011'),
(12, 12, 'VNPAY', 7490000, 'SUCCEEDED', 'TXN-0012'),
(13, 13, 'COD', 1290000, 'FAILED', NULL),
(14, 14, 'VNPAY', 180000, 'SUCCEEDED', 'TXN-0014'),
(15, 15, 'PAYPAL', 1590000, 'SUCCEEDED', 'PAY-0015'),
(16, 16, 'COD', 320000, 'SUCCEEDED', NULL),
(17, 17, 'VNPAY', 12990000, 'INITIATED', 'TXN-0017'),
(18, 18, 'COD', 690000, 'SUCCEEDED', NULL),
(19, 19, 'PAYPAL', 2490000, 'SUCCEEDED', 'PAY-0019'),
(20, 20, 'VNPAY', 10990000, 'INITIATED', 'TXN-0020'),
(21, 21, 'COD', 490000, 'SUCCEEDED', NULL),
(22, 22, 'VNPAY', 210000, 'SUCCEEDED', 'TXN-0022'),
(23, 23, 'PAYPAL', 690000, 'SUCCEEDED', 'PAY-0023'),
(24, 24, 'COD', 2490000, 'SUCCEEDED', NULL),
(25, 25, 'COD', 390000, 'INITIATED', NULL);

-- 12. REVIEWS (15)
INSERT INTO reviews (id, user_id, product_id, rating, comment) VALUES
(1, 2, 1, 5, 'Great phone, smooth and fast.'),
(2, 2, 7, 4, 'Very useful book for developers.'),
(3, 3, 10, 5, 'Nice dress, good quality.'),
(4, 4, 3, 4, 'Laptop is strong, great for dev.'),
(5, 4, 9, 3, 'Okay for the price.'),
(6, 5, 8, 5, 'Classic patterns, must-read.'),
(7, 6, 5, 4, 'Good headphones and battery.'),
(8, 7, 14, 5, 'Camera takes sharp photos.'),
(9, 8, 11, 4, 'Tablet screen is nice.'),
(10, 9, 20, 5, 'Studio sound is very accurate.'),
(11, 10, 21, 4, 'Action cam works great.'),
(12, 11, 22, 5, 'Tablet Pro is super smooth.'),
(13, 12, 23, 3, 'Sneakers look good.'),
(14, 13, 24, 4, 'Warm hoodie.'),
(15, 14, 25, 5, 'Great story, very engaging.');

-- 13. WISHLISTS (15)
INSERT INTO wishlists (id, user_id, product_id) VALUES
(1,2,1),(2,2,7),(3,3,3),(4,4,10),
(5,5,14),(6,6,5),(7,7,11),(8,8,22),(9,9,23),
(10,10,21),(11,11,4),(12,12,15),(13,13,8),(14,14,19),(15,15,20);

-- 14. RECENT_VIEWS (15)
INSERT INTO recent_views (id, user_id, product_id) VALUES
(1,2,1),(2,2,3),(3,3,7),(4,3,10),(5,4,5),
(6,5,14),(7,6,11),(8,7,22),(9,8,21),(10,9,23),
(11,10,24),(12,11,25),(13,12,20),(14,13,19),(15,14,18);

-- 15. SEARCH_LOGS (15)
INSERT INTO search_logs (id, user_id, keyword) VALUES
(1,2,'smartphone'),
(2,2,'smartphone x20'),
(3,3,'laptop pro 14'),
(4,3,'clean code'),
(5,4,'women dress'),
(6,NULL,'wireless headphones'),
(7,NULL,'power bank 20000mah'),
(8,NULL,'men tshirt'),
(9,7,'tablet pro 11'),
(10,8,'mirrorless camera'),
(11,9,'magsafe case'),
(12,10,'hoodie'),
(13,11,'sneakers'),
(14,12,'action camera'),
(15,NULL,'best laptop for dev');

-- 16. VOUCHERS (25)
INSERT INTO vouchers (id, code, name, description, discount_type, discount_value, max_discount_amount, min_order_total, start_at, end_at, usage_limit_global, usage_limit_user, status) VALUES
(1,'WELCOME10','Welcome 10% off','10% off for new users','PERCENT',10,100000,500000,'2025-01-01 00:00:00','2026-01-01 00:00:00',1000,5,'ACTIVE'),
(2,'FREESHIP300','Free Shipping 300k','Free ship for orders from 300k','FREESHIP',NULL,NULL,300000,'2025-01-01 00:00:00','2026-01-01 00:00:00',5000,10,'ACTIVE'),
(3,'FLASH20','Flash Sale 20%','20% off (limited)','PERCENT',20,200000,300000,'2025-11-01 00:00:00','2025-12-01 00:00:00',500,2,'ACTIVE'),
(4,'SAVE50K','Save 50k','50k off for 500k+','FIXED',50000,NULL,500000,'2025-01-01 00:00:00','2026-01-01 00:00:00',2000,5,'ACTIVE'),
(5,'SAVE100K','Save 100k','100k off for 1m+','FIXED',100000,NULL,1000000,'2025-01-01 00:00:00','2026-01-01 00:00:00',1000,2,'ACTIVE'),
(6,'TECH15','Tech 15%','15% off electronics','PERCENT',15,150000,600000,'2025-01-01 00:00:00','2026-01-01 00:00:00',1200,3,'ACTIVE'),
(7,'BOOK10','Book 10%','10% off books','PERCENT',10,80000,200000,'2025-01-01 00:00:00','2026-01-01 00:00:00',3000,10,'ACTIVE'),
(8,'FREESHIP500','FreeShip 500k','Free ship 500k+','FREESHIP',NULL,NULL,500000,'2025-01-01 00:00:00','2026-01-01 00:00:00',8000,10,'ACTIVE'),
(9,'FASHION12','Fashion 12%','12% off fashion','PERCENT',12,120000,400000,'2025-01-01 00:00:00','2026-01-01 00:00:00',1500,3,'ACTIVE'),
(10,'NEWYEAR25','New Year 25%','25% new year promo','PERCENT',25,250000,500000,'2025-12-25 00:00:00','2026-01-10 00:00:00',700,1,'ACTIVE'),
(11,'MEGA30','Mega 30%','30% off (inactive demo)','PERCENT',30,300000,900000,'2025-06-01 00:00:00','2025-07-01 00:00:00',400,1,'INACTIVE'),
(12,'EXPIRED5','Expired 5%','Expired voucher demo','PERCENT',5,50000,200000,'2024-01-01 00:00:00','2024-02-01 00:00:00',100,1,'EXPIRED'),
(13,'SHIPFREE','Ship Free','Free ship for all orders','FREESHIP',NULL,NULL,0,'2025-01-01 00:00:00','2025-12-31 23:59:59',10000,50,'ACTIVE'),
(14,'APP20K','App 20k','20k off when buying on app','FIXED',20000,NULL,150000,'2025-01-01 00:00:00','2026-01-01 00:00:00',5000,10,'ACTIVE'),
(15,'WELCOME5','Welcome 5%','5% off for new users','PERCENT',5,50000,200000,'2025-01-01 00:00:00','2026-01-01 00:00:00',5000,5,'ACTIVE'),
(16,'TOPRATED10','Top Rated 10%','10% off top rated items','PERCENT',10,120000,400000,'2025-01-01 00:00:00','2026-01-01 00:00:00',1500,2,'ACTIVE'),
(17,'PAYDAY70K','Payday 70k','70k off 700k+','FIXED',70000,NULL,700000,'2025-01-25 00:00:00','2025-02-05 00:00:00',900,1,'ACTIVE'),
(18,'GADGET8','Gadget 8%','8% off accessories','PERCENT',8,80000,250000,'2025-01-01 00:00:00','2026-01-01 00:00:00',2500,5,'ACTIVE'),
(19,'BULK5','Bulk 5%','5% off big orders','PERCENT',5,200000,2000000,'2025-01-01 00:00:00','2026-01-01 00:00:00',300,1,'ACTIVE'),
(20,'LAPTOP100K','Laptop 100k','100k off laptops 10m+','FIXED',100000,NULL,10000000,'2025-01-01 00:00:00','2026-01-01 00:00:00',800,1,'ACTIVE'),
(21,'CAMERA5','Camera 5%','5% off cameras','PERCENT',5,150000,1000000,'2025-01-01 00:00:00','2026-01-01 00:00:00',900,2,'ACTIVE'),
(22,'SNEAKER20K','Sneaker 20k','20k off shoes','FIXED',20000,NULL,300000,'2025-01-01 00:00:00','2026-01-01 00:00:00',4000,5,'ACTIVE'),
(23,'SERUM8','Skincare 8%','8% off skincare','PERCENT',8,60000,200000,'2025-01-01 00:00:00','2026-01-01 00:00:00',2500,5,'ACTIVE'),
(24,'ACCESSORY30K','Accessory 30k','30k off accessories','FIXED',30000,NULL,300000,'2025-01-01 00:00:00','2026-01-01 00:00:00',3500,5,'ACTIVE'),
(25,'VIP18','VIP 18%','18% VIP promo','PERCENT',18,180000,800000,'2025-01-01 00:00:00','2026-01-01 00:00:00',500,1,'ACTIVE');

-- 17. VOUCHER_USES (15)
INSERT INTO voucher_uses (id, voucher_id, user_id, order_id, discount_amount) VALUES
(1,1,2,1,100000),
(2,2,3,2,30000),
(3,3,4,3,200000),
(4,4,5,4,50000),
(5,5,6,5,100000),
(6,6,7,6,150000),
(7,7,8,7,80000),
(8,8,9,8,30000),
(9,9,10,9,120000),
(10,10,11,10,250000),
(11,14,12,11,20000),
(12,15,13,12,50000),
(13,18,14,13,80000),
(14,20,15,14,100000),
(15,24,16,15,30000);

-- 18. NOTIFICATIONS (15)
INSERT INTO notifications (id, user_id, title, message, type, reference_id, reference_type, is_read, is_hidden) VALUES
(1,2,'Welcome!','Thanks for joining our store.','SYSTEM',NULL,NULL,FALSE,FALSE),
(2,2,'Order #1 paid','Your order has been paid successfully.','ORDER',1,'ORDER',FALSE,FALSE),
(3,4,'Order #3 shipping','Your order is on the way.','ORDER',3,'ORDER',FALSE,FALSE),
(4,3,'Review received','Your review has been recorded.','REVIEW',2,'REVIEW',FALSE,FALSE),
(5,5,'Flash Sale','Up to 20% off today.','PROMOTION',NULL,NULL,FALSE,FALSE),
(6,6,'New voucher SAVE50K','Use SAVE50K for 50k off.','VOUCHER',NULL,NULL,FALSE,FALSE),
(7,7,'Order #6 paid','Payment succeeded.','PAYMENT',6,'ORDER',FALSE,FALSE),
(8,8,'Order #7 processing','Your order is being processed.','ORDER',7,'ORDER',FALSE,FALSE),
(9,9,'New arrivals','Check out our newest products.','PROMOTION',NULL,NULL,FALSE,FALSE),
(10,10,'Leave a review','Review your recent purchase to earn perks.','REVIEW',NULL,NULL,FALSE,FALSE),
(11,11,'Payment pending','Payment is awaiting confirmation.','PAYMENT',17,'ORDER',FALSE,FALSE),
(12,12,'Shipping update','Your order was handed to carrier.','ORDER',NULL,NULL,FALSE,FALSE),
(13,13,'System update','We updated our policy and terms.','SYSTEM',NULL,NULL,FALSE,FALSE),
(14,14,'Voucher FREESHIP500','Free shipping for 500k+ orders.','VOUCHER',NULL,NULL,FALSE,FALSE),
(15,15,'Weekend promo','Special discounts this weekend.','PROMOTION',NULL,NULL,FALSE,FALSE);

-- 19. REFRESH_TOKENS (15)
INSERT INTO refresh_tokens (id, token, expiry_date, user_id) VALUES
(1,'rt_0001_admin','2026-12-31 00:00:00',1),
(2,'rt_0002_u2','2026-12-31 00:00:00',2),
(3,'rt_0003_u3','2026-12-31 00:00:00',3),
(4,'rt_0004_u4','2026-12-31 00:00:00',4),
(5,'rt_0005_u5','2026-12-31 00:00:00',5),
(6,'rt_0006_u6','2026-12-31 00:00:00',6),
(7,'rt_0007_u7','2026-12-31 00:00:00',7),
(8,'rt_0008_u8','2026-12-31 00:00:00',8),
(9,'rt_0009_u9','2026-12-31 00:00:00',9),
(10,'rt_0010_u10','2026-12-31 00:00:00',10),
(11,'rt_0011_u11','2026-12-31 00:00:00',11),
(12,'rt_0012_u12','2026-12-31 00:00:00',12),
(13,'rt_0013_u13','2026-12-31 00:00:00',13),
(14,'rt_0014_u14','2026-12-31 00:00:00',14),
(15,'rt_0015_u15','2026-12-31 00:00:00',15);
