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
(6, 'david', 'david@example.com', '$2a$12$WbR5mX9bpcrLfAMzx5LnqeI8eJfJVf1iR1njZu6wUpks/H4I8P97i', 'David Le', '0977555444', TRUE);

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

-- 6. PRODUCT_IMAGES
INSERT INTO product_images (id, product_id, url, is_primary) VALUES 
(1, 1, 'https://cdn.example.com/images/smartphone-x20.jpg', TRUE),
(2, 3, 'https://cdn.example.com/images/laptop-pro-14.jpg', TRUE),
(3, 7, 'https://cdn.example.com/images/clean-code.jpg', TRUE),
(4, 9, 'https://cdn.example.com/images/men-tshirt.jpg', TRUE),
(5, 10, 'https://cdn.example.com/images/women-dress.jpg', TRUE);

-- 7. WAREHOUSES
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
