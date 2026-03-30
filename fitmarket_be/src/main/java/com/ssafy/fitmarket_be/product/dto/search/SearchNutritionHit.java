package com.ssafy.fitmarket_be.product.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchNutritionHit {
    private Float calories;
    private Float protein;
    private Float carbs;
    private Float fat;
}
