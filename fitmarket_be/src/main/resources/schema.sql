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
    `deleted_date` TIMESTAMP NOT NULL,
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
    `name` VARCHAR(100) NOT NULL DEFAULT 'pending',
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 주문
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_approval_status_id` BIGINT NOT NULL,
    `address_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `order_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ship_date` TIMESTAMP NOT NULL,
    `due_date` TIMESTAMP NOT NULL,
    `price` BIGINT NOT NULL,
    `comment` VARCHAR(255) NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_orders_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_orders_status` FOREIGN KEY (`order_approval_status_id`) REFERENCES `order_approval_status` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 주문 상품
CREATE TABLE `order_products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `price` BIGINT NOT NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_products_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_products_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 초기 데이터 삽입
-- ============================================
INSERT INTO `order_approval_status` (`name`) VALUES
('pending'), ('confirmed'), ('shipping'), ('delivered'), ('cancelled');

-- ============================================
-- 샘플 데이터 (개발/테스트용)
-- ============================================

-- 샘플 사용자 (판매자 역할)
INSERT INTO `users` (`id`, `name`, `email`, `password`, `phone`, `role`)
VALUES (1, '김영웅', 'seller@example.com', '{noop}password123', '010-1234-5678', 'USER');

-- 샘플 카테고리
INSERT INTO `product_categories` (`id`, `name`) VALUES
(1, '도시락'),
(2, '밀키트');

-- 샘플 식품 정보 (영양소)
INSERT INTO `food` (
    `id`, `code`, `name`, `calories`, `protein`, `carbs`, `fat`,
    `sodium`, `sugars`, `fiber`, `saturated_fat`, `trans_fat`, `calcium`
) VALUES (
    1, 'FD-001', '그린 샐러드 도시락',
    '320', '18', '35', '12',
    '450', '8', '6', '2.5', '0', '120'
);

-- 추가 표준 식품 (샘플)
INSERT INTO `food` (
    `id`, `code`, `name`, `calories`, `protein`, `carbs`, `fat`,
    `sodium`, `sugars`, `fiber`, `saturated_fat`, `trans_fat`, `calcium`
) VALUES
(2, 'FD-002', '그린 스무디', '210', '10', '28', '6', '90', '12', '4', '1', '0', '80'),
(3, 'FD-003', '연어 샐러드', '340', '22', '20', '15', '520', '6', '5', '3', '0', '140'),
(4, 'FD-004', '퀴노아 치킨 볼', '420', '30', '40', '12', '610', '5', '6', '2', '0', '160'),
(5, 'FD-005', '쉬림프 아보카도 볼', '380', '26', '32', '14', '540', '4', '5', '2', '0', '150'),
(6, 'FD-006', '지중해 샐러드', '310', '16', '25', '14', '430', '6', '5', '3', '0', '130'),
(7, 'FD-007', '채소 패턴 샐러드', '290', '12', '30', '10', '360', '7', '4', '2', '0', '110'),
(8, 'FD-008', '헬시 치킨 밀프렙', '450', '32', '42', '15', '680', '5', '6', '3', '0', '170');

-- 샘플 상품
INSERT INTO `products` (
    `user_id`, `product_category_id`, `name`, `description`, `price`, `stock`, `image_url`, `food_id`
) VALUES (
    1, 1, '그린 샐러드 도시락', '신선한 채소와 닭가슴살을 담은 샐러드 도시락',
    8500, 50, '/uploads/fresh-green-salad-bowl.png', 1
);

-- 추가 샘플 상품 (업로드된 이미지 기준)
INSERT INTO `products` (
    `user_id`, `product_category_id`, `name`, `description`, `price`, `stock`, `image_url`, `food_id`
) VALUES
(1, 1, '그린 스무디', '신선한 채소와 과일을 담은 클렌즈 스무디', 5900, 80, '/uploads/green-berry-smoothie.png', 2),
(1, 1, '연어 샐러드', '훈제 연어와 아보카도를 곁들인 영양 샐러드', 9800, 60, '/uploads/fresh-vegetables-pattern.png', 3),
(1, 2, '퀴노아 치킨 볼', '퀴노아와 치킨을 듬뿍 넣은 포만감 있는 밀프렙', 10200, 70, '/uploads/quinoa-chicken-bowl-healthy.png', 4),
(1, 2, '쉬림프 아보카도 볼', '통새우와 아보카도가 어우러진 고단백 한 그릇', 11200, 55, '/uploads/shrimp-avocado-bowl.png', 5),
(1, 1, '지중해 샐러드', '페타치즈와 올리브가 들어간 지중해풍 샐러드', 9200, 65, '/uploads/mediterranean-salad.png', 6),
(1, 2, '헬시 치킨 밀프렙', '닭가슴살과 구운 채소로 준비한 밀프렙 세트', 10500, 75, '/uploads/healthy-chicken-meal-prep.png', 8);

SELECT 'FitMarket 데이터베이스 초기화 완료 (Strict Mode)!' AS message;
