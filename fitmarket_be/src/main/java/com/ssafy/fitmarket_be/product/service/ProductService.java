package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.dto.*;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    /**
     * 상품 목록 조회 (페이징).
     */
    public PageResponse<ProductListResponse> getProducts(Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 20 : size;

        int offset = (safePage - 1) * safeSize;

        List<Product> products = productMapper.selectProducts(safeSize, offset);
        List<ProductListResponse> content = products.stream()
            .map(ProductListResponse::from)
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
    public ProductCreateResponse createProduct(ProductCreateRequest request) {
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

        return ProductCreateResponse.from(product);
    }

    /**
     * 상품 수정.
     */
    @Transactional
    public ProductUpdateResponse updateProduct(Long productId, ProductUpdateRequest request) {
        // 상품 수정
        productMapper.updateProduct(
            productId,
            request.name(),
            request.categoryId(),
            request.price(),
            request.description(),
            request.stock(),
            request.imageUrl()
        );

        // 수정된 상품 조회
        Product product = productMapper.selectProductById(productId);

        return ProductUpdateResponse.from(product);
    }

    /**
     * 상품 삭제 (소프트 삭제).
     */
    @Transactional
    public void deleteProduct(Long productId) {
        productMapper.deleteProduct(productId);
    }

    /**
     * 상품 상세 조회 (조회 시 review_count + 1).
     */
    @Transactional
    public ProductDetailResponse getProductDetail(Long productId) {
        productMapper.incrementReviewCount(productId);
        Product product = productMapper.selectProductById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다: " + productId);
        }
        return ProductDetailResponse.from(product);
    }

    /**
     * 판매자의 상품 목록 조회 (userId).
     */
    public List<ProductListResponse> getSellerProducts(Long userId) {
        List<Product> products = productMapper.selectProductsByUserId(userId);
        return products.stream()
            .map(ProductListResponse::from)
            .toList();
    }

    /**
     * 베스트 상품 조회 (평점/리뷰순).
     */
    public PageResponse<ProductListResponse> getBestProducts(Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 12 : size;

        int offset = (safePage - 1) * safeSize;
        List<Product> products = productMapper.selectBestProducts(safeSize, offset);
        List<ProductListResponse> content = products.stream()
            .map(ProductListResponse::from)
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
     * 신상품 조회 (최신순).
     */
    public PageResponse<ProductListResponse> getNewProducts(Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 12 : size;

        int offset = (safePage - 1) * safeSize;
        List<Product> products = productMapper.selectNewProducts(safeSize, offset);
        List<ProductListResponse> content = products.stream()
            .map(ProductListResponse::from)
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
