package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.dto.ProductResponse;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> getProducts(Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 20 : size;

        int offset = (safePage - 1) * safeSize;

        List<Product> products = productMapper.selectProducts(safeSize, offset);
        List<ProductResponse> content = products.stream()
            .map(ProductResponse::from)
            .toList();

        long totalElements = productMapper.countProducts();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        boolean hasNext = safePage < totalPages;
        boolean hasPrevious = safePage > 1;

        return new PageResponse<>(
            content,
            safePage,
            safeSize,
            totalElements,
            totalPages,
            hasNext,
            hasPrevious
        );
    }
}
