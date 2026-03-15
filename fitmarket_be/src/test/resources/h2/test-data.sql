-- Test seed data for mapper tests

-- order_approval_status (referenced by orders FK)
INSERT INTO order_approval_status (id, name) VALUES (1, 'pending_approval');
INSERT INTO order_approval_status (id, name) VALUES (2, 'approved');
INSERT INTO order_approval_status (id, name) VALUES (3, 'rejected');
INSERT INTO order_approval_status (id, name) VALUES (4, 'cancelled');
INSERT INTO order_approval_status (id, name) VALUES (5, 'shipping');
INSERT INTO order_approval_status (id, name) VALUES (6, 'delivered');

-- users
INSERT INTO users (id, name, email, password, phone, role)
VALUES (1, '테스트유저', 'user@test.com', 'encodedPassword', '01012345678', 'USER');

INSERT INTO users (id, name, email, password, phone, role)
VALUES (2, '다른유저', 'other@test.com', 'encodedPassword2', '01099990000', 'USER');

-- food
INSERT INTO food (id, code, name, calories, protein, carbs, fat)
VALUES (1, 'FD-001', '테스트식품', '320', '18', '35', '12');

-- product_categories
INSERT INTO product_categories (id, name) VALUES (1, '도시락');

-- products (user_id=1, category_id=1, food_id=1)
INSERT INTO products (id, user_id, product_category_id, name, price, stock, food_id)
VALUES (1, 1, 1, '테스트상품', 8500, 50, 1);

INSERT INTO products (id, user_id, product_category_id, name, price, stock, food_id)
VALUES (2, 1, 1, '테스트상품2', 9000, 30, 1);

-- shopping_cart_products (cart item id=1: userId=1, productId=1, qty=2)
INSERT INTO shopping_cart_products (id, user_id, product_id, quantity)
VALUES (1, 1, 1, 2);

-- cart item id=2: userId=1, productId=2, qty=3 (for softDelete + count test)
INSERT INTO shopping_cart_products (id, user_id, product_id, quantity)
VALUES (2, 1, 2, 3);

-- address (needed for orders FK)
INSERT INTO address (id, recipient, phone, address_line, address_line_detail)
VALUES (1, '수령인', '01011112222', '서울시 강남구', '101호');

-- orders (id=1: userId=1, orderNumber='ORD-001', status=pending_approval)
INSERT INTO orders (id, order_number, order_mode, order_approval_status_id,
                    address_id, address_snapshot, user_id,
                    merchandise_amount, shipping_fee, discount_amount, total_amount,
                    payment_status)
VALUES (1, 'ORD-001', 'CART', 1,
        1, '{}', 1,
        10000, 3000, 0, 13000,
        'PENDING');
