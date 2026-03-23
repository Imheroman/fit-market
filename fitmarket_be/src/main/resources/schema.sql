-- ============================================
-- FitMarket Database 초기화 스크립트 (Strict Mode)
-- ============================================


DROP DATABASE IF EXISTS fitmarket;
CREATE DATABASE fitmarket
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE fitmarket;

-- ============================================
-- 기존 테이블 삭제
-- ============================================
DROP TABLE IF EXISTS `sellers`; -- [RENAMED]
DROP TABLE IF EXISTS `payment_refunds`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_addresses`;
DROP TABLE IF EXISTS `order_products`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `shopping_cart_products`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `product_categories`;
DROP TABLE IF EXISTS `food`;
DROP TABLE IF EXISTS `user_address`;
DROP TABLE IF EXISTS `address`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `order_approval_status`;

-- ============================================
-- 테이블 생성
-- ============================================

-- 1. 회원 테이블 (사업자 정보 컬럼 삭제됨)
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `role` VARCHAR(100) NOT NULL DEFAULT 'USER', -- 권한 체크용 (로그인 시 JOIN 방지)

    -- [삭제됨] business_name, business_number는 이제 여기 없습니다.

    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 판매자 신청 및 정보 테이블 (정보의 유일한 원천)
CREATE TABLE `sellers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `business_name` VARCHAR(100) NOT NULL,
    `business_number` VARCHAR(50) NOT NULL,
    `business_type` VARCHAR(20) NOT NULL DEFAULT 'individual',
    `contact_phone` VARCHAR(30) NOT NULL,
    `business_address` VARCHAR(255) NOT NULL,
    `introduction` VARCHAR(500) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, approved, rejected
    `review_note` VARCHAR(255) NULL,     -- 거절/승인 사유
    `reviewed_by` BIGINT NULL,           -- 처리한 관리자
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seller_app_user` (`user_id`),           -- 유저당 1개의 신청 정보만 유지 (1:1 Extension)
    UNIQUE KEY `uk_seller_app_biz_num` (`business_number`), -- 사업자 번호 중복 방지
    CONSTRAINT `fk_seller_app_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_seller_app_admin` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 주소 테이블
CREATE TABLE `address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(16) NULL,
    `recipient` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `memo` VARCHAR(200) NULL,
    `postal_code` VARCHAR(15) NULL,
    `address_line` VARCHAR(255) NOT NULL,
    `address_line_detail` VARCHAR(255) NOT NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 사용자-주소 연결 테이블
CREATE TABLE `user_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `address_id` BIGINT NOT NULL,
    `is_main` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '대표 주소 여부(1: 대표)',
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_user_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_user_address_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 표준 식품 DB
CREATE TABLE `food` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `food_category_major` VARCHAR(255) NULL,
    `food_category_minor` VARCHAR(255) NULL,
    `calories` VARCHAR(255) NULL,
    `protein` VARCHAR(255) NULL,
    `carbs` VARCHAR(255) NULL,
    `fat` VARCHAR(255) NULL,
    `sodium` VARCHAR(255) NULL,
    `sugars` VARCHAR(255) NULL,
    `fiber` VARCHAR(255) NULL,
    `saturated_fat` VARCHAR(255) NULL,
    `trans_fat` VARCHAR(255) NULL,
    `calcium` VARCHAR(255) NULL,
    `created_date` VARCHAR(255) NULL,
    `modified_date` VARCHAR(255) NULL,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_food_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 상품 카테고리
CREATE TABLE `product_categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT NULL,
    `name` VARCHAR(100) NOT NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_product_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `product_categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 상품 테이블
CREATE TABLE `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_category_id` BIGINT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL,
    `price` BIGINT NOT NULL,
    `stock` INT NOT NULL,
    `image_url` VARCHAR(255) NULL,
    `weight_g` INT NOT NULL DEFAULT 100,
    `rating` DOUBLE NOT NULL DEFAULT 0,
    `review_count` INT NOT NULL DEFAULT 0,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    `food_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_products_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_products_category` FOREIGN KEY (`product_category_id`) REFERENCES `product_categories` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_products_food` FOREIGN KEY (`food_id`) REFERENCES `food` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 장바구니
CREATE TABLE `shopping_cart_products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `quantity` INT NOT NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 주문 승인 상태
CREATE TABLE `order_approval_status` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL DEFAULT 'pending_approval',
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_approval_status_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 주문
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_number` VARCHAR(40) NOT NULL,
    `order_mode` ENUM('CART', 'DIRECT') NOT NULL,
    `order_approval_status_id` BIGINT NOT NULL DEFAULT 1,
    `address_id` BIGINT NOT NULL,
    `address_snapshot` JSON NOT NULL,
    `items_snapshot` JSON NULL,
    `user_id` BIGINT NOT NULL,
    `order_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ship_date` TIMESTAMP NULL,
    `due_date` TIMESTAMP NULL,
    `merchandise_amount` BIGINT NOT NULL,
    `shipping_fee` BIGINT NOT NULL DEFAULT 0,
    `discount_amount` BIGINT NOT NULL DEFAULT 0,
    `total_amount` BIGINT NOT NULL,
    `payment_status` ENUM('PENDING', 'PAID', 'REFUNDED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    `comment` VARCHAR(255) NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orders_order_number` (`order_number`),
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_orders_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_orders_status` FOREIGN KEY (`order_approval_status_id`) REFERENCES `order_approval_status` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 주문 상품
CREATE TABLE `order_products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `cart_item_id` BIGINT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` BIGINT NOT NULL,
    `total_price` BIGINT NOT NULL,
    `option_info` JSON NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_products_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_products_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 결제
CREATE TABLE `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `payment_key` VARCHAR(120) NOT NULL,
    `provider` VARCHAR(30) NOT NULL,
    `method` VARCHAR(50) NULL,
    `status` ENUM('PENDING', 'PAID', 'REFUNDED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    `amount` BIGINT NOT NULL,
    `approved_at` TIMESTAMP NULL,
    `failed_code` VARCHAR(50) NULL,
    `failed_message` VARCHAR(255) NULL,
    `raw_response` JSON NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payments_order` (`order_id`),
    UNIQUE KEY `uk_payments_payment_key` (`payment_key`),
    CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 결제 환불 이력
CREATE TABLE `payment_refunds` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `payment_id` BIGINT NOT NULL,
    `amount` BIGINT NOT NULL,
    `reason` VARCHAR(255) NULL,
    `processed_at` TIMESTAMP NULL,
    `status` ENUM('pending', 'succeeded', 'failed') NOT NULL DEFAULT 'pending',
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_payment_refunds_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. 주문 주소 이력 (옵션)
CREATE TABLE `order_addresses` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `recipient` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `postal_code` VARCHAR(15) NULL,
    `address_line` VARCHAR(255) NOT NULL,
    `address_line_detail` VARCHAR(255) NOT NULL,
    `memo` VARCHAR(200) NULL,
    `is_current` TINYINT(1) NOT NULL DEFAULT 1,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_addresses_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. 반품, 교환, 환불 요청
CREATE TABLE `order_return_exchanges` (
                                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                                          `order_id` BIGINT NOT NULL,
                                          `type` ENUM('REFUND', 'RETURN', 'EXCHANGE') NOT NULL,
                                          `reason` ENUM('QUALITY_ISSUE', 'CHANGE_OF_MIND', 'DAMAGED', 'WRONG_ITEM', 'OTHER') NOT NULL,
                                          `detail` VARCHAR(500) NOT NULL,
                                          `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
                                          `requested_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          `processed_at` TIMESTAMP NULL,
                                          `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          `deleted_date` TIMESTAMP NULL,
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `uk_order_return_exchanges_order` (`order_id`),
                                          CONSTRAINT `fk_order_return_exchanges_order`
                                              FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 초기 데이터 삽입
-- ============================================
INSERT INTO `order_approval_status` (`name`) VALUES
('pending_approval'),
('approved'),
('rejected'),
('cancelled'),
('shipping'),
('delivered');

-- ============================================
-- 샘플 데이터 (개발/테스트용)
-- ============================================

-- 샘플 사용자 (판매자 역할)
INSERT INTO `users` (`id`, `name`, `email`, `password`, `phone`, `role`)
VALUES (1, '김영웅', 'seller@example.com', '{noop}password123', '010-1234-5678', 'USER');

-- 샘플 카테고리
INSERT INTO `product_categories` (`id`, `name`) VALUES
(1, '도시락'),
(2, '밀키트'),
(3, '샐러드'),
(4, '단백질 보충식'),
(5, '스무디');

-- 표준 식품 데이터 (전국통합식품영양성분정보 기반)
INSERT INTO `food` (
    `id`, `code`, `name`, `food_category_major`, `food_category_minor`,
    `calories`, `protein`, `carbs`, `fat`, `sodium`, `sugars`, `fiber`, `saturated_fat`, `trans_fat`, `calcium`
) VALUES
(1, 'D401-001000000-0001', '계란 덮밥', '밥류', '', '169', '5.00', '27.22', '3.69', '105', '0.81', '0.8', '0.87', '0', '13'),
(2, 'D301-001000000-0001', '계란덮밥', '밥류', '', '143', '4.31', '25.14', '2.82', '223', '3.07', '0.8', '0.61', '0.01', '17'),
(3, 'D601-060000000-0001', '낙지 덮밥', '밥류', '', '92', '3.80', '16.10', '1.23', '146', '2.35', '1.2', '0.22', '0', '12'),
(4, 'D401-008350000-0001', '넙치(광어)회덮밥_양념장', '밥류', '양념장', '77', '4.27', '12.64', '1.06', '85', '1.92', '0.8', '0.16', '0', '18'),
(5, 'D310-462000000-0001', '닭볶음(닭갈비)_간편조리세트_모짜렐라 치즈 닭갈비', '볶음류', '', '150', '11.59', '13.82', '5.41', '473', '6.51', '1.9', '2.10', '0.07', '101'),
(6, 'D110-462155200-0001', '닭볶음(닭갈비)_닭가슴살_피망', '볶음류', '닭가슴살', '122', '16.39', '1.38', '5.65', '490', '0.29', '2.6', '0.65', '0.03', '28'),
(7, 'D310-462000000-0002', '닭볶음(닭갈비)_간편조리세트_치즈 품은 닭갈비', '볶음류', '', '160', '9.50', '17.96', '5.62', '462', '4.45', '1.8', '2.15', '0.05', '127'),
(8, 'D506-308000000-0001', '굴 두부찌개', '찌개 및 전골류', '', '21', '2.20', '1.48', '0.66', '98', '0.44', '0.5', '0.10', '0', '29'),
(9, 'D714-669460000-0001', '닭가슴살 샐러드_드레싱', '생채·무침류', '드레싱', '37', '4.10', '2.03', '1.36', '56', '1.21', '0.7', '0.31', '0', '11'),
(10, 'D214-640000000-0002', '샐러드_그릴드 치킨 샐러드', '생채·무침류', '', '99', '6.40', '0', '0', '315', '4.93', '0', '0.84', '0', '0'),
(11, 'D214-640000000-0010', '샐러드_쉬림프에그 샐러드', '생채·무침류', '', '83', '6.92', '0', '0', '302', '4.40', '0', '1.07', '0', '0'),
(12, 'D214-640000000-0019', '샐러드_과일리코타 샐러드', '생채·무침류', '', '111', '3.20', '11.40', '5.80', '119', '8.40', '0', '2.80', '0', '0'),
(13, 'D214-640000000-0003', '샐러드_그릴드치킨 시저 샐러드', '생채·무침류', '', '104', '7.49', '0', '0', '322', '3.52', '0', '0.97', '0', '0'),
(14, 'D214-640000000-0047', '샐러드_콥샐러드', '생채·무침류', '', '108', '6.94', '0', '0', '259', '1.83', '0', '1.83', '0', '0'),
(15, 'D708-388000000-0001', '연어구이', '구이류', '', '138', '16.60', '0.20', '7.24', '850', '0.00', '0.0', '1.09', '0', '20'),
(16, 'D312-549070000-0001', '닭튀김_닭가슴살', '튀김류', '닭가슴살', '252', '21.00', '20.20', '9.70', '60', '0', '0', '0.00', '0.00', '6'),
(17, 'D102-096060000-0001', '샌드위치_닭가슴살', '빵 및 과자류', '닭가슴살', '240', '12.18', '20.96', '11.92', '438', '3.09', '2.4', '3.16', '0.17', '58'),
(18, 'D114-640080000-0001', '샐러드_닭가슴살', '생채·무침류', '닭가슴살', '135', '7.17', '5.36', '9.49', '88', '3.25', '3.1', '1.21', '0.03', '41'),
(19, 'D520-756000000-0001', '딸기바나나 스무디', '음료 및 차류', '', '54', '1.10', '11.75', '0.68', '11', '9.34', '0.9', '0.40', '0', '29'),
(20, 'D220-737000000-0800', '스무디_그릭요거트 스트로베리 블루베리 (R)', '음료 및 차류', '', '69', '1.48', '0', '0', '18', '13.32', '0', '0.21', '0', '0'),
(21, 'D220-737000000-0011', '스무디_그래놀라요거트프로스트 블루베리 (L)', '음료 및 차류', '', '137', '2.43', '0', '0', '80', '18.26', '0', '1.05', '0', '0'),
(22, 'D220-737000000-0790', '스무디_골드망고 스무디', '음료 및 차류', '', '35', '0.14', '8.61', '0.05', '4', '6.50', '0', '0.02', '0.00', '0');

-- 샘플 상품 (실제 영양 데이터 기반)
INSERT INTO `products` (
    `user_id`, `product_category_id`, `name`, `description`, `price`, `stock`, `image_url`, `weight_g`, `rating`, `review_count`, `food_id`
) VALUES
(1, 1, '계란 덮밥 도시락', '부드러운 계란과 양파가 어우러진 담백한 덮밥 도시락', 7500, 80, '/uploads/fresh-green-salad-bowl.png', 350, 4.3, 42, 1),
(1, 1, '잡곡 비빔밥 도시락', '영양 가득 잡곡밥에 신선한 나물을 곁들인 비빔밥', 8500, 60, '/uploads/quinoa-chicken-bowl-healthy.png', 400, 4.5, 67, 2),
(1, 1, '낙지 덮밥 도시락', '쫄깃한 낙지와 매콤한 양념의 저칼로리 덮밥', 9800, 45, '/uploads/healthy-chicken-meal-prep.png', 380, 4.2, 31, 3),
(1, 1, '광어회 덮밥 도시락', '신선한 광어회와 초장을 곁들인 프리미엄 회덮밥', 12500, 30, '/uploads/shrimp-avocado-bowl.png', 420, 4.7, 89, 4),
(1, 2, '모짜렐라 치즈 닭갈비 밀키트', '쭉 늘어나는 치즈와 매콤한 닭갈비의 조화', 11900, 70, '/uploads/healthy-chicken-meal-prep.png', 500, 4.6, 124, 5),
(1, 2, '닭가슴살 피망볶음 밀키트', '고단백 닭가슴살과 아삭한 피망의 간편 볶음', 9900, 85, '/uploads/quinoa-chicken-bowl-healthy.png', 400, 4.4, 56, 6),
(1, 2, '치즈 품은 닭갈비 밀키트', '부드러운 치즈를 품은 달콤 매콤 닭갈비', 12500, 55, '/uploads/healthy-chicken-meal-prep.png', 550, 4.5, 93, 7),
(1, 2, '굴 두부찌개 밀키트', '바다 향 가득한 굴과 부드러운 두부의 건강 찌개', 10500, 40, '/uploads/mediterranean-salad.png', 600, 4.3, 38, 8),
(1, 3, '닭가슴살 드레싱 샐러드', '저지방 닭가슴살과 신선한 채소에 특제 드레싱', 8900, 90, '/uploads/fresh-green-salad-bowl.png', 250, 4.5, 156, 9),
(1, 3, '그릴드 치킨 샐러드', '직화 구이 치킨과 풍성한 채소의 고단백 샐러드', 9200, 75, '/uploads/fresh-vegetables-pattern.png', 280, 4.6, 203, 10),
(1, 3, '쉬림프에그 샐러드', '통새우와 완숙 달걀이 어우러진 프리미엄 샐러드', 10800, 55, '/uploads/shrimp-avocado-bowl.png', 300, 4.4, 78, 11),
(1, 3, '과일 리코타 샐러드', '신선한 과일과 크리미한 리코타치즈 샐러드', 9500, 65, '/uploads/mediterranean-salad.png', 260, 4.3, 94, 12),
(1, 3, '그릴드치킨 시저 샐러드', '바삭한 크루통과 파마산치즈의 클래식 시저 샐러드', 9800, 70, '/uploads/fresh-vegetables-pattern.png', 290, 4.7, 187, 13),
(1, 3, '콥 샐러드', '아보카도, 베이컨, 치즈가 듬뿍 들어간 콥 샐러드', 10200, 60, '/uploads/fresh-green-salad-bowl.png', 320, 4.5, 112, 14),
(1, 4, '연어 구이 세트', '오메가3 풍부한 노르웨이산 연어 구이', 13500, 40, '/uploads/shrimp-avocado-bowl.png', 200, 4.8, 215, 15),
(1, 4, '닭가슴살 텐더', '바삭하게 튀긴 고단백 닭가슴살 텐더', 8500, 100, '/uploads/quinoa-chicken-bowl-healthy.png', 250, 4.4, 143, 16),
(1, 4, '닭가슴살 샌드위치', '통밀빵에 두툼한 닭가슴살을 넣은 고단백 샌드위치', 6900, 90, '/uploads/healthy-chicken-meal-prep.png', 220, 4.3, 67, 17),
(1, 4, '닭가슴살 샐러드 밀프렙', '일주일 식단 관리를 위한 닭가슴살 샐러드 세트', 11500, 50, '/uploads/fresh-green-salad-bowl.png', 300, 4.6, 178, 18),
(1, 5, '딸기바나나 스무디', '신선한 딸기와 바나나를 갈아 만든 비타민 스무디', 5500, 120, '/uploads/green-berry-smoothie.png', 350, 4.4, 89, 19),
(1, 5, '그릭요거트 베리 스무디', '그릭요거트에 딸기와 블루베리를 블렌딩한 건강 음료', 6200, 100, '/uploads/green-berry-smoothie.png', 400, 4.6, 134, 20),
(1, 5, '그래놀라 요거트 블루베리 스무디', '바삭한 그래놀라와 블루베리 요거트의 포만감 스무디', 6800, 80, '/uploads/green-berry-smoothie.png', 450, 4.5, 76, 21),
(1, 5, '골드망고 스무디', '달콤한 골드망고를 듬뿍 넣은 트로피컬 스무디', 5900, 95, '/uploads/green-berry-smoothie.png', 380, 4.3, 52, 22);

SELECT 'FitMarket 데이터베이스 초기화 완료 (Strict Mode)!' AS message;
