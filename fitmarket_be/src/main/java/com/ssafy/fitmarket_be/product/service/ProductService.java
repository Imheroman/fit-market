package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.product.dto.ProductListResponse;
import com.ssafy.fitmarket_be.product.dto.ProductListResponse.Pagination;
import com.ssafy.fitmarket_be.product.dto.ProductListResponse.ProductItem;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public ProductListResponse getProducts(Integer page, Integer size) {
        // OFFSET 계산: page는 0부터 시작
        int offset = page * size;

        // 상품 목록 조회
        List<ProductItem> items = productMapper.selectProducts(size, offset);


        // 전체 상품 개수 조회
        Long totalElements = productMapper.countProducts();

        // 페이지네이션 메타 정보 계산
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        Pagination pagination = new Pagination(
            page,
            totalPages,
            totalElements,
            size,
            hasNext,
            hasPrevious
        );

        return new ProductListResponse(items, pagination);
    }
}
