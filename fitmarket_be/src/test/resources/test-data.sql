-- order_approval_status (FK dependency)
INSERT INTO order_approval_status (id, name) VALUES
(1, 'pending_approval'),
(2, 'approved'),
(3, 'rejected'),
(4, 'cancelled'),
(5, 'shipping'),
(6, 'delivered');

-- users (3명: USER, ADMIN, SELLER)
-- BCrypt of "password123" (BCryptPasswordEncoder().encode("password123") 으로 생성)
INSERT INTO users (id, name, email, password, phone, role) VALUES
(1, '테스트유저', 'user@test.com', '$2a$10$2PPDxpUKhq3FipmhvcSEIeQLhKArl2I3gP2LMoehOa/J2JTLT8DoW', '010-1111-1111', 'USER'),
(2, '테스트관리자', 'admin@test.com', '$2a$10$2PPDxpUKhq3FipmhvcSEIeQLhKArl2I3gP2LMoehOa/J2JTLT8DoW', '010-2222-2222', 'ADMIN'),
(3, '테스트판매자', 'seller@test.com', '$2a$10$2PPDxpUKhq3FipmhvcSEIeQLhKArl2I3gP2LMoehOa/J2JTLT8DoW', '010-3333-3333', 'SELLER');

-- product_categories
INSERT INTO product_categories (id, name) VALUES (1, '도시락'), (2, '밀키트');

-- food
INSERT INTO food (id, code, name, calories, protein, carbs, fat, sodium, sugars, fiber, saturated_fat, trans_fat, calcium)
VALUES (1, 'FD-001', '테스트식품', '300', '15', '30', '10', '400', '5', '3', '2', '0', '100');

-- products (seller userId=3 소유)
INSERT INTO products (id, user_id, product_category_id, name, description, price, stock, food_id)
VALUES (1, 3, 1, '테스트상품', '테스트 상품 설명', 10000, 100, 1);

-- shopping_cart_products (장바구니 통합 테스트용: userId=1, productId=1)
INSERT INTO shopping_cart_products (id, user_id, product_id, quantity)
VALUES (1, 1, 1, 2);

-- address
INSERT INTO address (id, name, recipient, phone, memo, postal_code, address_line, address_line_detail)
VALUES (1, '집', '테스트수령인', '010-1234-5678', NULL, '12345', '서울시 강남구', '101호');

-- user_address
INSERT INTO user_address (id, user_id, address_id, is_main) VALUES (1, 1, 1, 1);

-- orders
-- ORD-001: userId=1, PENDING_APPROVAL (status_id=1) → 주문상세 조회 및 취소 테스트용
-- ORD-002: userId=1, SHIPPING (status_id=5) → 배송지 수정 불가 테스트용
-- ORD-003: userId=1, PENDING_APPROVAL (status_id=1) → 소프트 삭제 테스트용
INSERT INTO orders (id, order_number, order_mode, order_approval_status_id, address_id, address_snapshot, items_snapshot, user_id, merchandise_amount, shipping_fee, discount_amount, total_amount, payment_status)
VALUES
(1, 'ORD-001', 'DIRECT', 1, 1, '{"recipient":"테스트수령인","phone":"010-1234-5678","postalCode":"12345","addressLine":"서울시 강남구","addressLineDetail":"101호","memo":null}', '[]', 1, 10000, 0, 0, 10000, 'PAID'),
(2, 'ORD-002', 'DIRECT', 5, 1, '{"recipient":"테스트수령인","phone":"010-1234-5678","postalCode":"12345","addressLine":"서울시 강남구","addressLineDetail":"101호","memo":null}', '[]', 1, 10000, 0, 0, 10000, 'PAID'),
(3, 'ORD-003', 'DIRECT', 1, 1, '{"recipient":"테스트수령인","phone":"010-1234-5678","postalCode":"12345","addressLine":"서울시 강남구","addressLineDetail":"101호","memo":null}', '[]', 1, 10000, 0, 0, 10000, 'PAID');
