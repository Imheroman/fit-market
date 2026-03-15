#!/usr/bin/env python3
"""
seed-generator.py — 부하 테스트용 시드 데이터 생성기

생성 대상:
  - users     1개  (id=1,000,001)
  - sellers   1개  (id=1,000,001, user_id=1,000,001)
  - food  30,000개  (id=1,000,001 ~ 1,030,000)
  - products 30,000개 (id=1,000,001 ~ 1,030,000)

출력:
  load-test/product-cache/data/
    ├── sellers.csv     (user + seller 행 포함)
    ├── food.csv
    ├── products.csv
    └── seed-data.sql   (LOAD DATA LOCAL INFILE 방식)

ID 오프셋 전략:
  모든 테스트 데이터는 1,000,000번대부터 시작하여
  운영 데이터(1~999,999)와 충돌 없음.
"""

import os
import csv
import random
import math
from datetime import datetime

# ── 상수 ─────────────────────────────────────────────────────────────────────
BASE_ID        = 1_000_000
FOOD_COUNT     = 30_000
PRODUCT_COUNT  = 30_000
CATEGORY_IDS   = [1, 2, 3, 4, 5]   # 기존 카테고리 재사용

# BCrypt 해시 (평문: "loadtest1!")
SELLER_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lkii"

NOW = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

# 상품명 조합
NAME_PREFIXES = [
    "고단백", "저칼로리", "단백질", "다이어트", "헬시",
    "오가닉", "채소", "닭가슴살", "현미", "통밀",
]
NAME_SUFFIXES = [
    "도시락", "샐러드", "밀키트", "보충식", "스무디",
    "바", "쉐이크", "볼", "컵밥", "죽",
]

# 식품 카테고리
FOOD_MAJOR_CATEGORIES = ["단백질식품", "채소류", "곡류", "유제품", "과일류"]
FOOD_MINOR_CATEGORIES = ["가공품", "신선식품", "냉동식품", "건조식품", "음료"]

# 디렉토리 설정
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR   = os.path.join(SCRIPT_DIR, "data")
os.makedirs(DATA_DIR, exist_ok=True)

random.seed(42)  # 재현 가능한 랜덤 시드


# ── 헬퍼 ─────────────────────────────────────────────────────────────────────

def rand_price():
    """3,000 ~ 30,000원, 1,000원 단위"""
    return random.randint(3, 30) * 1_000


def rand_stock():
    """10 ~ 500개"""
    return random.randint(10, 500)


def rand_weight():
    """150 ~ 500g"""
    return random.randint(150, 500)


def rand_rating():
    """1.0 ~ 5.0 (소수점 1자리)"""
    return round(random.uniform(1.0, 5.0), 1)


def rand_review_count():
    """지수분포 적용 (대부분 낮음, 가끔 높음), 최대 500"""
    val = int(random.expovariate(1 / 30))
    return min(val, 500)


def rand_nutrient(lo, hi):
    """lo ~ hi 사이 실수 문자열 (소수점 1자리)"""
    return str(round(random.uniform(lo, hi), 1))


# ── 1. users + sellers CSV ───────────────────────────────────────────────────

sellers_csv_path = os.path.join(DATA_DIR, "sellers.csv")

with open(sellers_csv_path, "w", newline="\n", encoding="utf-8") as f:
    writer = csv.writer(f)

    # 헤더 없이 데이터만 (LOAD DATA LOCAL INFILE은 헤더 스킵 가능하나 명시적 컬럼 지정)
    # users 행: id,name,email,password,phone,role,created_date,modified_date,deleted_date
    writer.writerow([
        BASE_ID + 1,                    # id
        "부하테스트판매자",               # name
        "loadtest@fitmarket.com",        # email
        SELLER_PASSWORD,                 # password
        "010-0000-0000",                 # phone
        "USER",                         # role
        NOW,                            # created_date
        NOW,                            # modified_date
        "",                             # deleted_date (NULL)
    ])

print(f"[1/4] sellers.csv 생성 완료: {sellers_csv_path}")


# ── 2. food CSV ──────────────────────────────────────────────────────────────

food_csv_path = os.path.join(DATA_DIR, "food.csv")

with open(food_csv_path, "w", newline="\n", encoding="utf-8") as f:
    writer = csv.writer(f)

    for i in range(FOOD_COUNT):
        food_id = BASE_ID + 1 + i
        major = random.choice(FOOD_MAJOR_CATEGORIES)
        minor = random.choice(FOOD_MINOR_CATEGORIES)

        writer.writerow([
            food_id,                            # id
            f"FD-LT-{food_id}",                 # code
            f"테스트식품-{i + 1:04d}",            # name
            major,                              # food_category_major
            minor,                              # food_category_minor
            rand_nutrient(50, 500),             # calories
            rand_nutrient(1, 50),               # protein
            rand_nutrient(5, 80),               # carbs
            rand_nutrient(1, 30),               # fat
            rand_nutrient(100, 2000),           # sodium
            rand_nutrient(0.5, 20),             # sugars
            rand_nutrient(0.5, 15),             # fiber
            rand_nutrient(0.5, 15),             # saturated_fat
            rand_nutrient(0, 2),                # trans_fat
            rand_nutrient(10, 400),             # calcium
        ])

print(f"[2/4] food.csv 생성 완료: {food_csv_path} ({FOOD_COUNT}개)")


# ── 3. products CSV ──────────────────────────────────────────────────────────

products_csv_path = os.path.join(DATA_DIR, "products.csv")

with open(products_csv_path, "w", newline="\n", encoding="utf-8") as f:
    writer = csv.writer(f)

    for i in range(PRODUCT_COUNT):
        product_id  = BASE_ID + 1 + i
        food_id     = BASE_ID + 1 + random.randint(0, FOOD_COUNT - 1)
        category_id = random.choice(CATEGORY_IDS)
        prefix      = NAME_PREFIXES[i % len(NAME_PREFIXES)]
        suffix      = NAME_SUFFIXES[(i // len(NAME_PREFIXES)) % len(NAME_SUFFIXES)]
        name        = f"{prefix} {suffix} {i + 1:04d}호"
        description = f"{prefix} 성분이 풍부한 건강한 {suffix}입니다. 엄선된 재료로 제조하였습니다. (상품번호: {i + 1:04d})"
        image_url   = f"https://via.placeholder.com/300x300?text=Product{i + 1}"

        writer.writerow([
            product_id,                         # id
            BASE_ID + 1,                        # user_id (판매자)
            category_id,                        # product_category_id
            name,                               # name
            description,                        # description
            rand_price(),                       # price
            rand_stock(),                       # stock
            image_url,                          # image_url
            rand_weight(),                      # weight_g
            rand_rating(),                      # rating
            rand_review_count(),                # review_count
            food_id,                            # food_id
            NOW,                                # created_date
            NOW,                                # modified_date
            "",                                 # deleted_date (NULL)
        ])

print(f"[3/4] products.csv 생성 완료: {products_csv_path} ({PRODUCT_COUNT}개)")


# ── 4. seed-data.sql 생성 ────────────────────────────────────────────────────

sql_path          = os.path.join(DATA_DIR, "seed-data.sql")
sellers_abs       = os.path.abspath(sellers_csv_path).replace("\\", "/")
food_abs          = os.path.abspath(food_csv_path).replace("\\", "/")
products_abs      = os.path.abspath(products_csv_path).replace("\\", "/")

sql_content = f"""-- seed-data.sql
-- 자동 생성: {NOW}
-- 부하 테스트용 시드 데이터 (LOAD DATA LOCAL INFILE)
-- ID 오프셋: 1,000,000번대 (운영 데이터와 충돌 없음)
--
-- 실행 방법:
--   mysql -h 127.0.0.1 -P 3307 -u loadtest -ploadtest fitmarket_loadtest \\
--         --local-infile=1 < seed-data.sql

SET FOREIGN_KEY_CHECKS = 0;
SET GLOBAL local_infile = 1;

-- ── 1. users (판매자 계정) ─────────────────────────────────────────────────
-- 컬럼: id, name, email, password, phone, role, created_date, modified_date, deleted_date
LOAD DATA LOCAL INFILE '{sellers_abs}'
  INTO TABLE users
  FIELDS TERMINATED BY ','
  OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\\n'
  (@id, @name, @email, @password, @phone, @role, @created_date, @modified_date, @deleted_date)
  SET
    id           = @id,
    name         = @name,
    email        = @email,
    password     = @password,
    phone        = @phone,
    role         = @role,
    created_date = @created_date,
    modified_date = @modified_date,
    deleted_date = NULLIF(@deleted_date, '');

-- ── 2. sellers ────────────────────────────────────────────────────────────
-- sellers 행은 users CSV에서 user_id를 재사용하여 직접 INSERT
INSERT IGNORE INTO sellers
  (id, user_id, business_name, business_number, business_type,
   contact_phone, business_address, introduction, status, created_date, modified_date)
VALUES
  ({BASE_ID + 1}, {BASE_ID + 1}, '부하테스트마켓', 'LT-0000001', 'INDIVIDUAL',
   '010-0000-0000', '서울시 테스트구 부하동 1번지', '부하 테스트 전용 판매자 계정입니다.', 'approved',
   '{NOW}', '{NOW}');

-- ── 3. food (영양소 데이터) ───────────────────────────────────────────────
-- 컬럼: id, code, name, food_category_major, food_category_minor,
--        calories, protein, carbs, fat, sodium, sugars, fiber,
--        saturated_fat, trans_fat, calcium
LOAD DATA LOCAL INFILE '{food_abs}'
  INTO TABLE food
  FIELDS TERMINATED BY ','
  OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\\n'
  (id, code, name, food_category_major, food_category_minor,
   calories, protein, carbs, fat, sodium, sugars, fiber,
   saturated_fat, trans_fat, calcium);

-- ── 4. products (상품 30,000개) ───────────────────────────────────────────
-- 컬럼: id, user_id, product_category_id, name, description, price, stock,
--        image_url, weight_g, rating, review_count, food_id,
--        created_date, modified_date, deleted_date
LOAD DATA LOCAL INFILE '{products_abs}'
  INTO TABLE products
  FIELDS TERMINATED BY ','
  OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\\n'
  (@id, @user_id, @product_category_id, @name, @description, @price, @stock,
   @image_url, @weight_g, @rating, @review_count, @food_id,
   @created_date, @modified_date, @deleted_date)
  SET
    id                  = @id,
    user_id             = @user_id,
    product_category_id = @product_category_id,
    name                = @name,
    description         = @description,
    price               = @price,
    stock               = @stock,
    image_url           = @image_url,
    weight_g            = @weight_g,
    rating              = @rating,
    review_count        = @review_count,
    food_id             = @food_id,
    created_date        = @created_date,
    modified_date       = @modified_date,
    deleted_date        = NULLIF(@deleted_date, '');

SET FOREIGN_KEY_CHECKS = 1;

-- 적재 확인
SELECT 'users'    AS tbl, COUNT(*) AS cnt FROM users    WHERE id >= 1000000
UNION ALL
SELECT 'sellers'  AS tbl, COUNT(*) AS cnt FROM sellers  WHERE id >= 1000000
UNION ALL
SELECT 'food'     AS tbl, COUNT(*) AS cnt FROM food     WHERE id >= 1000000
UNION ALL
SELECT 'products' AS tbl, COUNT(*) AS cnt FROM products WHERE id >= 1000000;
"""

with open(sql_path, "w", encoding="utf-8") as f:
    f.write(sql_content)

print(f"[4/4] seed-data.sql 생성 완료: {sql_path}")
print()
print("=" * 60)
print("시드 데이터 생성 완료!")
print(f"  users:    1개  (id={BASE_ID + 1})")
print(f"  sellers:  1개  (id={BASE_ID + 1})")
print(f"  food:  {FOOD_COUNT}개  (id={BASE_ID + 1}~{BASE_ID + FOOD_COUNT})")
print(f"  products: {PRODUCT_COUNT}개  (id={BASE_ID + 1}~{BASE_ID + PRODUCT_COUNT})")
print()
print("다음 명령으로 MySQL에 적재하세요:")
print("  mysql -h 127.0.0.1 -P 3307 -u loadtest -ploadtest fitmarket_loadtest \\")
print(f"        --local-infile=1 < {sql_path}")
print("=" * 60)
