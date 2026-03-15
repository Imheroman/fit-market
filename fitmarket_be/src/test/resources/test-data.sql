-- order_approval_status (FK dependency)
INSERT INTO order_approval_status (id, name) VALUES
(1, 'pending_approval'),
(2, 'approved'),
(3, 'rejected'),
(4, 'cancelled'),
(5, 'shipping'),
(6, 'delivered');

-- users (3명: USER, ADMIN, SELLER)
INSERT INTO users (id, name, email, password, phone, role) VALUES
(1, '테스트유저', 'user@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '010-1111-1111', 'USER'),
(2, '테스트관리자', 'admin@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '010-2222-2222', 'ADMIN'),
(3, '테스트판매자', 'seller@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '010-3333-3333', 'SELLER');
-- BCrypt of "password123"

-- product_categories
INSERT INTO product_categories (id, name) VALUES (1, '도시락'), (2, '밀키트');

-- food
INSERT INTO food (id, code, name, calories, protein, carbs, fat, sodium, sugars, fiber, saturated_fat, trans_fat, calcium)
VALUES (1, 'FD-001', '테스트식품', '300', '15', '30', '10', '400', '5', '3', '2', '0', '100');

-- products (seller userId=3 소유)
INSERT INTO products (id, user_id, product_category_id, name, description, price, stock, food_id)
VALUES (1, 3, 1, '테스트상품', '테스트 상품 설명', 10000, 100, 1);
