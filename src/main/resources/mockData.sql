-- ============================================================
-- TB001 Mock Data — 10 rows per table, FK-safe insert order
-- Run order matters: users / product_category → product → orders
-- ============================================================

-- 1) users (10) — no FK
INSERT INTO users (user_id, username) OVERRIDING SYSTEM VALUE VALUES
  (1,  'alice'),
  (2,  'bob'),
  (3,  'carol'),
  (4,  'david'),
  (5,  'eve'),
  (6,  'frank'),
  (7,  'grace'),
  (8,  'henry'),
  (9,  'ivy'),
  (10, 'jack');

-- 2) product_category (10) — no FK
INSERT INTO product_category (category_id, category_name, tax_rate) OVERRIDING SYSTEM VALUE VALUES
  (1,  '食品',     0.0500),
  (2,  '3C',       0.1000),
  (3,  '服飾',     0.0800),
  (4,  '書籍',     0.0000),
  (5,  '飲料',     0.0500),
  (6,  '美妝',     0.1000),
  (7,  '玩具',     0.0800),
  (8,  '家具',     0.1000),
  (9,  '文具',     0.0500),
  (10, '運動用品', 0.0800);

-- 3) product (10) — FK → product_category.category_id
INSERT INTO product (product_id, product_category_id, unit_price) OVERRIDING SYSTEM VALUE VALUES
  (1,  1,    50.00),
  (2,  2,  1999.00),
  (3,  3,   599.00),
  (4,  4,   320.00),
  (5,  5,    75.00),
  (6,  6,   880.00),
  (7,  7,   450.00),
  (8,  8,  4500.00),
  (9,  9,    25.00),
  (10, 10, 1200.00);

-- 4) orders (10) — FK → users.user_id + product.product_id
INSERT INTO orders (order_id, user_id, product_id, order_amount) OVERRIDING SYSTEM VALUE VALUES
  (1,  1,  1,  3),
  (2,  2,  2,  1),
  (3,  3,  3,  2),
  (4,  4,  4,  5),
  (5,  5,  5, 10),
  (6,  6,  6,  2),
  (7,  7,  7,  4),
  (8,  8,  8,  1),
  (9,  9,  9, 20),
  (10, 10, 10, 3);


SELECT setval(pg_get_serial_sequence('users',            'user_id'),     10);
SELECT setval(pg_get_serial_sequence('product_category', 'category_id'), 10);
SELECT setval(pg_get_serial_sequence('product',          'product_id'),  10);
SELECT setval(pg_get_serial_sequence('orders',           'order_id'),    10);



--== 驗證 ==

SELECT COUNT(*) FROM users;             -- 應為 10
SELECT COUNT(*) FROM product_category;  -- 應為 10
SELECT COUNT(*) FROM product;           -- 應為 10
SELECT COUNT(*) FROM orders;            -- 應為 10

-- 驗 FK
-- 10 筆
SELECT o.order_id, u.username, p.product_id, pc.category_name, p.unit_price, pc.tax_rate, o.order_amount
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN product p ON o.product_id = p.product_id
JOIN product_category pc ON p.product_category_id = pc.category_id
ORDER BY o.order_id;
