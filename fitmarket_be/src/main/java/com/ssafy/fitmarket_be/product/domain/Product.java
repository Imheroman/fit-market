package com.ssafy.fitmarket_be.product.domain;

import lombok.Getter;
import lombok.ToString;

/**
 * 상품 도메인 모델 (불변).
 * DB 조회 전용 객체로, MyBatis에서 생성됩니다.
 */
@Getter
@ToString
public final class Product {
    private final Long id;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final Long price;
    private final String imageUrl;
    private final double rating;
    private final int reviewCount;
    private final Nutrition nutrition;

    /**
     * MyBatis용 생성자 (package-private).
     * 영양 정보를 개별 파라미터로 받아 Nutrition 객체를 생성합니다.
     */
    Product(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        Long price,
        String imageUrl,
        Double rating,        // primitive → 래퍼 타입
        Integer reviewCount,  // primitive → 래퍼 타입
        Integer calories,     // primitive → 래퍼 타입
        Integer protein,      // primitive → 래퍼 타입
        Integer carbs,        // primitive → 래퍼 타입
        Integer fat           // primitive → 래퍼 타입
    ) {
        // 유효성 검증
        validatePrice(price);
        validateRating(rating);
        validateReviewCount(reviewCount);

        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.nutrition = new Nutrition(calories, protein, carbs, fat);
    }

    // ========== 검증 메서드 ==========

    private void validatePrice(Long price) {
        if (price != null && price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다: " + price);
        }
    }

    private void validateRating(Double rating) {
        if (rating != null && (rating < 0 || rating > 5.0)) {
            throw new IllegalArgumentException("평점은 0~5 사이여야 합니다: " + rating);
        }
    }

    private void validateReviewCount(Integer reviewCount) {
        if (reviewCount != null && reviewCount < 0) {
            throw new IllegalArgumentException("리뷰 개수는 0 이상이어야 합니다: " + reviewCount);
        }
    }

    // ========== 비즈니스 로직 ==========

    /**
     * 고단백 상품인지 판단합니다 (단백질 20g 이상).
     * Nutrition에게 위임합니다 (Tell, Don't Ask).
     */
    public boolean isHighProtein() {
        return nutrition.isHighProtein();
    }

    /**
     * 저칼로리 상품인지 판단합니다 (칼로리 300kcal 이하).
     * Nutrition에게 위임합니다 (Tell, Don't Ask).
     */
    public boolean isLowCalorie() {
        return nutrition.isLowCalorie();
    }

    /**
     * 영양 등급을 반환합니다.
     * A: 고단백 + 저칼로리
     * B: 고단백만
     * C: 그 외
     */
    public String getNutritionGrade() {
        if (nutrition.isHighProtein() && nutrition.isLowCalorie()) {
            return "A";
        }
        if (nutrition.isHighProtein()) {
            return "B";
        }
        return "C";
    }

    /**
     * 할인된 가격을 계산합니다.
     * @param discountRate 할인율 (0~100)
     * @return 할인 적용된 가격
     */
    public Long calculateDiscountedPrice(int discountRate) {
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("할인율은 0~100 사이여야 합니다: " + discountRate);
        }
        return price * (100 - discountRate) / 100;
    }

    /**
     * 지정된 금액보다 비싼지 판단합니다.
     */
    public boolean isExpensiveThan(Long threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("비교 금액은 null일 수 없습니다");
        }
        return price > threshold;
    }

    /**
     * 평점이 높은지 판단합니다 (4.0 이상).
     */
    public boolean hasHighRating() {
        return rating >= 4.0;
    }

    /**
     * 인기 상품인지 판단합니다 (평점 4.0 이상 + 리뷰 10개 이상).
     */
    public boolean isPopular() {
        return hasHighRating() && reviewCount >= 10;
    }
}
