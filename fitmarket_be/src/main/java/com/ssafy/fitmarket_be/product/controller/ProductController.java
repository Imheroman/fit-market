package com.ssafy.fitmarket_be.product.controller;

import com.ssafy.fitmarket_be.global.common.ApiResponse;
import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.dto.*;
import com.ssafy.fitmarket_be.product.service.ProductSearchService;
import com.ssafy.fitmarket_be.product.service.ProductService;
import com.ssafy.fitmarket_be.ranking.service.ProductRankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final ProductRankingService rankingService;

    /**
     * 상품 목록 조회 (페이징, 필터링).
     * keyword가 있으면 ES 검색 (Fallback: MySQL), 없으면 기존 MySQL 조회.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProducts(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword
    ) {
        PageResponse<ProductListResponse> response;
        if (keyword != null && !keyword.isBlank()) {
            response = productSearchService.search(keyword.trim(), categoryId, page, size);
        } else {
            response = productService.getProducts(page, size, categoryId, null);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 등록.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
        @AuthenticationPrincipal(expression = "id") Long userId,
        @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductCreateResponse response = productService.createProduct(userId, request);

        return ResponseEntity
            .created(URI.create("/products/" + response.id()))
            .body(ApiResponse.success(response));
    }

    /**
     * 상품 수정.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductUpdateResponse>> updateProduct(
        @AuthenticationPrincipal(expression = "id") Long userId,
        @PathVariable Long id,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductUpdateResponse response = productService.updateProduct(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 상세 조회 (조회 시 review_count 증가).
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Long id) {
        ProductDetailResponse response = productService.getProductDetail(id);
        productService.incrementViewCount(id);
        rankingService.incrementScore(id, 1.0);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 삭제.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
        @AuthenticationPrincipal(expression = "id") Long userId,
        @PathVariable Long id
    ) {
        productService.deleteProduct(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 판매자의 상품 목록 조회.
     */
    @GetMapping("/seller")
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> getSellerProducts(
        @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        List<ProductListResponse> response = productService.getSellerProducts(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 베스트 상품 조회.
     */
    @GetMapping("/best")
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getBestProducts(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "12") Integer size
    ) {
        PageResponse<ProductListResponse> response = productService.getBestProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 신상품 조회.
     */
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getNewProducts(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "12") Integer size
    ) {
        PageResponse<ProductListResponse> response = productService.getNewProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
