package com.ssafy.fitmarket_be.product.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionInfo {

    private Float calories;
    private Float protein;
    private Float carbs;
    private Float fat;
}
