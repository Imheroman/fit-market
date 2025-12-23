package com.ssafy.fitmarket_be.food.repository;

import com.ssafy.fitmarket_be.food.domain.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FoodMapper {

    /**
     * 전체 식품 조회 (LLM 매칭용).
     * 주의: 16,000개 이상의 데이터가 있으므로 limit 사용 권장.
     */
    List<Food> selectAllFoods(@Param("limit") Integer limit);

    /**
     * 식품 ID로 조회.
     */
    Food selectFoodById(@Param("id") Long id);
}
