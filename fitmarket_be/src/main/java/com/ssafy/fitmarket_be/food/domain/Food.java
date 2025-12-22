package com.ssafy.fitmarket_be.food.domain;

import lombok.Getter;
import lombok.ToString;

/**
 * 식품 DB 도메인 모델 (불변).
 * DB 조회 전용 객체로, MyBatis에서 생성됩니다.
 * schema.sql의 food 테이블 구조에 맞춰 설계됨.
 */
@Getter
@ToString
public final class Food {
    private final Long id;
    private final String code;
    private final String name;
    private final String foodCategoryMajor;
    private final String foodCategoryMinor;
    private final String calories;
    private final String protein;
    private final String carbs;
    private final String fat;
    private final String sodium;
    private final String sugars;
    private final String fiber;
    private final String saturatedFat;
    private final String transFat;
    private final String calcium;

    /**
     * MyBatis용 생성자 (package-private).
     */
    Food(
        Long id,
        String code,
        String name,
        String foodCategoryMajor,
        String foodCategoryMinor,
        String calories,
        String protein,
        String carbs,
        String fat,
        String sodium,
        String sugars,
        String fiber,
        String saturatedFat,
        String transFat,
        String calcium
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.foodCategoryMajor = foodCategoryMajor;
        this.foodCategoryMinor = foodCategoryMinor;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.sodium = sodium;
        this.sugars = sugars;
        this.fiber = fiber;
        this.saturatedFat = saturatedFat;
        this.transFat = transFat;
        this.calcium = calcium;
    }

    // 숫자 필드를 위한 편의 메서드
    public Integer getCaloriesAsInt() {
        try {
            return calories != null ? Integer.parseInt(calories) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getProteinAsDouble() {
        try {
            return protein != null ? Double.parseDouble(protein) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getCarbsAsDouble() {
        try {
            return carbs != null ? Double.parseDouble(carbs) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getFatAsDouble() {
        try {
            return fat != null ? Double.parseDouble(fat) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
