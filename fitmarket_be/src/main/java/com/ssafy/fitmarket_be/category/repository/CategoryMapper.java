package com.ssafy.fitmarket_be.category.repository;

import com.ssafy.fitmarket_be.category.domain.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 모든 카테고리 조회 (상품 개수 포함).
     */
    List<ProductCategory> selectAllCategoriesWithProductCount();
}