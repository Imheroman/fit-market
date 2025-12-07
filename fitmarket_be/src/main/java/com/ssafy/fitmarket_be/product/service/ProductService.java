package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.dto.ProductCreateRequest;
import com.ssafy.fitmarket_be.product.dto.ProductResponse;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    /**
     * 상품 목록 조회 (페이징).
     */
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

    /**
     * 상품 등록.
     * TODO: AI 영양 정보 계산 기능 추가 예정
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        // TODO: 나중에 AI로 상품명/설명 기반 식품 DB 매칭
        // 현재는 고정값 사용 (food_id = 1)
        Long foodId = 1L;

        // 상품 등록
        productMapper.insertProduct(
            request.userId(),
            request.categoryId(),
            request.name(),
            request.description(),
            request.price(),
            request.stock(),
            request.imageUrl(),
            foodId
        );

        // 방금 등록한 상품 조회
        Long productId = productMapper.selectLastInsertId();
        Product product = productMapper.selectProductById(productId);

        return ProductResponse.from(product);
    }

    /**
     * 판매자의 상품 목록 조회 (email).
     */
    public List<ProductResponse> getSellerProductsByEmail(String email) {
        List<Product> products = productMapper.selectProductsByEmail(email);
        return products.stream()
            .map(ProductResponse::from)
            .toList();
    }
}
