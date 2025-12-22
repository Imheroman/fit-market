package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.ai.service.LLMService;
import com.ssafy.fitmarket_be.food.domain.Food;
import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.dto.*;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final LLMService llmService;
    private final com.ssafy.fitmarket_be.ai.service.FoodVectorStoreService foodVectorStoreService;

    /**
     * 상품 목록 조회 (페이징, 필터링).
     * categoryId와 keyword를 동시에 적용 가능합니다.
     */
    public PageResponse<ProductListResponse> getProducts(Integer page, Integer size, Long categoryId, String keyword) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 20 : size;

        int offset = (safePage - 1) * safeSize;

        String normalizedKeyword = normalizeKeyword(keyword);

        // 카테고리와 키워드 조합에 따라 다른 쿼리 실행
        List<Product> products = productMapper.selectProductsWithFilters(categoryId, normalizedKeyword, safeSize, offset);
        List<ProductListResponse> content = products.stream()
            .map(ProductListResponse::from)
            .toList();

        long totalElements = productMapper.countProductsWithFilters(categoryId, normalizedKeyword);
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 상품 등록.
     * RAG를 사용하여 상품명과 유사한 식품 후보를 찾고, LLM으로 최종 매칭합니다.
     */
    @Transactional
    public ProductCreateResponse createProduct(Long userId, ProductCreateRequest request) {
        // RAG: 벡터 검색으로 상위 50개 유사 식품 추출 (토큰 대폭 절감!)
        List<Food> similarFoods =
            foodVectorStoreService.searchSimilarFoods(request.name(), 50);

        // LLM으로 최종 매칭 (50개만 전달하므로 토큰 99% 절감)
        Long foodId = llmService.findBestMatch(request.name(), similarFoods);

        // 상품 등록
        productMapper.insertProduct(
            userId,
            request.categoryId(),
            request.name(),
            request.description(),
            request.price(),
            request.weightG(),
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
            request.weightG(),
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
