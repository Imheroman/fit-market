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
DROP TABLE IF EXISTS `seller_applications`; -- [NEW]
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
CREATE TABLE `seller_applications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `business_name` VARCHAR(100) NOT NULL,
    `business_number` VARCHAR(50) NOT NULL,

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
    `postal_code` VARCHAR(15) NULL,
    `address_line` VARCHAR(255) NOT NULL,
    `address_line_detail` VARCHAR(255) NOT NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    CONSTRAINT `fk_user_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_address_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE CASCADE
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
    `image_url` VARCHAR(50) NULL,
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

-- 샘플 상품
INSERT INTO `products` (
    `user_id`, `product_category_id`, `name`, `description`, `price`, `stock`, `image_url`, `food_id`
) VALUES (
    1, 1, '그린 샐러드 도시락', '신선한 채소와 닭가슴살을 담은 샐러드 도시락',
    8500, 50, '/fresh-green-salad-bowl.png', 1
);

SELECT 'FitMarket 데이터베이스 초기화 완료 (Strict Mode)!' AS message;
