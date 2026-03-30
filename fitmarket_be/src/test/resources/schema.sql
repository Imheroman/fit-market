-- ============================================
-- FitMarket H2 Test Schema (MySQL mode)
-- ============================================

-- Drop in reverse FK dependency order
DROP TABLE IF EXISTS `order_return_exchanges`;
DROP TABLE IF EXISTS `order_addresses`;
DROP TABLE IF EXISTS `payment_refunds`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_products`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `shopping_cart_products`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `product_categories`;
DROP TABLE IF EXISTS `food`;
DROP TABLE IF EXISTS `user_address`;
DROP TABLE IF EXISTS `address`;
DROP TABLE IF EXISTS `sellers`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `order_approval_status`;

-- 1. 주문 승인 상태
CREATE TABLE `order_approval_status` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL DEFAULT 'pending_approval',
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_approval_status_name` (`name`)
);

-- 2. 회원 테이블
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `role` VARCHAR(100) NOT NULL DEFAULT 'USER',
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`)
);

-- 3. 판매자 신청 및 정보 테이블
CREATE TABLE `sellers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `business_name` VARCHAR(100) NOT NULL,
    `business_number` VARCHAR(50) NOT NULL,
    `business_type` VARCHAR(20) NOT NULL DEFAULT 'individual',
    `contact_phone` VARCHAR(30) NOT NULL,
    `business_address` VARCHAR(255) NOT NULL,
    `introduction` VARCHAR(500) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending',
    `review_note` VARCHAR(255) NULL,
    `reviewed_by` BIGINT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seller_app_user` (`user_id`),
    UNIQUE KEY `uk_seller_app_biz_num` (`business_number`),
    CONSTRAINT `fk_seller_app_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_seller_app_admin` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
);

-- 4. 주소 테이블
CREATE TABLE `address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(16) NULL,
    `recipient` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `memo` VARCHAR(200) NULL,
    `postal_code` VARCHAR(15) NULL,
    `address_line` VARCHAR(255) NOT NULL,
    `address_line_detail` VARCHAR(255) NOT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`)
);

-- 5. 사용자-주소 연결 테이블
CREATE TABLE `user_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `address_id` BIGINT NOT NULL,
    `is_main` TINYINT(1) NOT NULL DEFAULT 0,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_user_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_user_address_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`)
);

-- 6. 표준 식품 DB
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
);

-- 7. 상품 카테고리
CREATE TABLE `product_categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT NULL,
    `name` VARCHAR(100) NOT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_product_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `product_categories` (`id`) ON DELETE CASCADE
);

-- 8. 상품 테이블
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
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    `food_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_products_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_products_category` FOREIGN KEY (`product_category_id`) REFERENCES `product_categories` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_products_food` FOREIGN KEY (`food_id`) REFERENCES `food` (`id`) ON DELETE RESTRICT
);

-- 9. 장바구니
CREATE TABLE `shopping_cart_products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `quantity` INT NOT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
);

-- 10. 주문
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_number` VARCHAR(40) NOT NULL,
    `order_mode` VARCHAR(50) NOT NULL,
    `order_approval_status_id` BIGINT NOT NULL DEFAULT 1,
    `address_id` BIGINT NOT NULL,
    `address_snapshot` TEXT NOT NULL,
    `items_snapshot` TEXT NULL,
    `user_id` BIGINT NOT NULL,
    `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `ship_date` TIMESTAMP NULL,
    `due_date` TIMESTAMP NULL,
    `merchandise_amount` BIGINT NOT NULL,
    `shipping_fee` BIGINT NOT NULL DEFAULT 0,
    `discount_amount` BIGINT NOT NULL DEFAULT 0,
    `total_amount` BIGINT NOT NULL,
    `payment_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    `comment` VARCHAR(255) NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orders_order_number` (`order_number`),
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_orders_address` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_orders_status` FOREIGN KEY (`order_approval_status_id`) REFERENCES `order_approval_status` (`id`) ON DELETE RESTRICT
);

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
    `option_info` TEXT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_products_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_products_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT
);

-- 12. 결제
CREATE TABLE `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `payment_key` VARCHAR(120) NOT NULL,
    `provider` VARCHAR(30) NOT NULL,
    `method` VARCHAR(50) NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    `amount` BIGINT NOT NULL,
    `approved_at` TIMESTAMP NULL,
    `failed_code` VARCHAR(50) NULL,
    `failed_message` VARCHAR(255) NULL,
    `raw_response` TEXT NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payments_order` (`order_id`),
    UNIQUE KEY `uk_payments_payment_key` (`payment_key`),
    CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
);

-- 13. 결제 환불 이력
CREATE TABLE `payment_refunds` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `payment_id` BIGINT NOT NULL,
    `amount` BIGINT NOT NULL,
    `reason` VARCHAR(255) NULL,
    `processed_at` TIMESTAMP NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'pending',
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_payment_refunds_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`) ON DELETE CASCADE
);

-- 14. 주문 주소 이력
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
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_addresses_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
);

-- 15. 반품, 교환, 환불 요청
CREATE TABLE `order_return_exchanges` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `reason` VARCHAR(50) NOT NULL,
    `detail` VARCHAR(500) NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    `requested_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `processed_at` TIMESTAMP NULL,
    `created_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `modified_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted_date` TIMESTAMP NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_return_exchanges_order` (`order_id`),
    CONSTRAINT `fk_order_return_exchanges_order`
        FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
);
