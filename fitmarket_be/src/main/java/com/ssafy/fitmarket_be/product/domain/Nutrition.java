package com.ssafy.fitmarket_be.product.domain;

import lombok.Getter;
import lombok.ToString;

/**
 * 영양 정보 값 객체 (불변).
 * Value Object 패턴을 따릅니다.
 */
@Getter
@ToString
public final class Nutrition {
    private final int calories;
    private final int protein;
    private final int carbs;
    private final int fat;

    /**
     * package-private 생성자.
     * Product 도메인 내부에서만 생성 가능합니다.
     */
    Nutrition(Integer calories, Integer protein, Integer carbs, Integer fat) {
        // null 체크 및 기본값 설정
        int safeCalories = (calories != null) ? calories : 0;
        int safeProtein = (protein != null) ? protein : 0;
        int safeCarbs = (carbs != null) ? carbs : 0;
        int safeFat = (fat != null) ? fat : 0;

        // 유효성 검증
        validateNutrient(safeCalories, "칼로리");
        validateNutrient(safeProtein, "단백질");
        validateNutrient(safeCarbs, "탄수화물");
        validateNutrient(safeFat, "지방");

        this.calories = safeCalories;
        this.protein = safeProtein;
        this.carbs = safeCarbs;
        this.fat = safeFat;
    }

    private void validateNutrient(int value, String nutrientName) {
        if (value < 0) {
            throw new IllegalArgumentException(nutrientName + "은(는) 0 이상이어야 합니다: " + value);
        }
    }

    // ========== Getter (ProductResponse 변환용) ==========

    public int getCalories() {
        return calories;
    }

    public int getProtein() {
        return protein;
    }

    public int getCarbs() {
        return carbs;
    }

    public int getFat() {
        return fat;
    }

    // ========== 비즈니스 로직 ==========

    /**
     * 고단백인지 판단합니다 (단백질 20g 이상).
     */
    public boolean isHighProtein() {
        return protein >= 20;
    }

    /**
     * 저칼로리인지 판단합니다 (칼로리 300kcal 이하).
     */
    public boolean isLowCalorie() {
        return calories <= 300;
    }

    /**
     * 영양소 기반 칼로리를 계산합니다.
     * 단백질: 4kcal/g, 탄수화물: 4kcal/g, 지방: 9kcal/g
     */
    public int calculateCaloriesFromMacros() {
        return (protein * 4) + (carbs * 4) + (fat * 9);
    }

    /**
     * 실제 칼로리와 계산 칼로리의 차이가 큰지 확인합니다.
     * (일부 제품은 식이섬유 등으로 인해 차이가 발생할 수 있음)
     */
    public boolean hasSignificantCalorieDifference() {
        int calculated = calculateCaloriesFromMacros();
        int difference = Math.abs(calories - calculated);
        return difference > 50; // 50kcal 이상 차이
    }

    /**
     * 단백질 비율을 반환합니다 (0~100).
     */
    public double getProteinRatio() {
        if (calories == 0) return 0;
        return (protein * 4.0 / calories) * 100;
    }

    /**
     * 탄수화물 비율을 반환합니다 (0~100).
     */
    public double getCarbsRatio() {
        if (calories == 0) return 0;
        return (carbs * 4.0 / calories) * 100;
    }

    /**
     * 지방 비율을 반환합니다 (0~100).
     */
    public double getFatRatio() {
        if (calories == 0) return 0;
        return (fat * 9.0 / calories) * 100;
    }

    /**
     * 저탄수화물 식품인지 확인합니다 (탄수화물 10g 이하).
     */
    public boolean isLowCarb() {
        return carbs <= 10;
    }

    /**
     * 저지방 식품인지 확인합니다 (지방 5g 이하).
     */
    public boolean isLowFat() {
        return fat <= 5;
    }
}
