package com.ssafy.fitmarket_be.category.service;

import com.ssafy.fitmarket_be.category.domain.ProductCategory;
import com.ssafy.fitmarket_be.category.dto.CategoryResponse;
import com.ssafy.fitmarket_be.category.repository.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 모든 카테고리 조회 (상품 개수 포함).
     */
    public List<CategoryResponse> getAllCategories() {
        List<ProductCategory> categories = categoryMapper.selectAllCategoriesWithProductCount();
        return categories.stream()
            .map(CategoryResponse::from)
            .toList();
    }
}