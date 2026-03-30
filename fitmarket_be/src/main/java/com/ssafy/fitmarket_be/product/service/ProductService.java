package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.ai.service.LLMService;
import com.ssafy.fitmarket_be.food.domain.Food;
import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.dto.*;
import com.ssafy.fitmarket_be.product.event.ProductEvent;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @org.springframework.beans.factory.annotation.Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    /**
     * 상품 목록 조회 (페이징, 필터링).
     * categoryId와 keyword를 동시에 적용 가능합니다.
     */
    @Cacheable(value = "products",
               key = "#categoryId + ':' + #keyword + ':' + #page + ':' + #size",
               sync = true)
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
    @CacheEvict(value = {"products", "best-products", "new-products", "categories"}, allEntries = true)
    @Transactional
    public ProductCreateResponse createProduct(Long userId, ProductCreateRequest request) {
        Long foodId = null;
        if (aiEnabled) {
            // RAG: 벡터 검색으로 상위 50개 유사 식품 추출 (토큰 대폭 절감!)
            List<Food> similarFoods =
                foodVectorStoreService.searchSimilarFoods(request.name(), 50);

            // LLM으로 최종 매칭 (50개만 전달하므로 토큰 99% 절감)
            foodId = llmService.findBestMatch(request.name(), similarFoods);
        }

        // 상품 등록
        ProductInsertCommand command = ProductInsertCommand.builder()
            .userId(userId)
            .categoryId(request.categoryId())
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .weightG(request.weightG())
            .stock(request.stock())
            .imageUrl(request.imageUrl())
            .foodId(foodId)
            .build();

        productMapper.insertProduct(command);

        // 방금 등록한 상품 조회
        Long productId = command.getId();  // MyBatis가 자동 세팅한 ID
        Product product = productMapper.selectProductById(productId);

        eventPublisher.publishEvent(new ProductEvent.Created(productId));
        return ProductCreateResponse.from(product);
    }

    /**
     * 상품 수정.
     */
    @Caching(evict = {
        @CacheEvict(value = {"products", "best-products", "new-products"}, allEntries = true),
        @CacheEvict(value = "product-detail", key = "#productId")
    })
    @Transactional
    public ProductUpdateResponse updateProduct(Long userId, Long productId, ProductUpdateRequest request) {
        Product product = productMapper.selectProductById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
        if (!productMapper.existsByIdAndUserId(productId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 상품을 수정할 권한이 없습니다.");
        }
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
        Product updated = productMapper.selectProductById(productId);

        eventPublisher.publishEvent(new ProductEvent.Updated(productId));
        return ProductUpdateResponse.from(updated);
    }

    /**
     * 상품 삭제 (소프트 삭제).
     */
    @Caching(evict = {
        @CacheEvict(value = {"products", "best-products", "new-products", "categories"}, allEntries = true),
        @CacheEvict(value = "product-detail", key = "#productId")
    })
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Product product = productMapper.selectProductById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
        if (!productMapper.existsByIdAndUserId(productId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 상품을 삭제할 권한이 없습니다.");
        }
        productMapper.deleteProduct(productId);
        eventPublisher.publishEvent(new ProductEvent.Deleted(productId));
    }

    /**
     * 상품 상세 조회 (읽기 전용 — 캐시 대상).
     * 조회수 증가는 {@link #incrementViewCount(Long)}에서 별도로 수행한다.
     */
    @Cacheable(value = "product-detail", key = "#productId")
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productMapper.selectProductById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다: " + productId);
        }
        return ProductDetailResponse.from(product);
    }

    /**
     * 상품 조회수를 1 증가시킨다 (캐시 비대상).
     */
    @Transactional
    public void incrementViewCount(Long productId) {
        productMapper.incrementViewCount(productId);
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
    @Cacheable(value = "best-products",
               key = "#page + ':' + #size",
               sync = true)
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
    @Cacheable(value = "new-products",
               key = "#page + ':' + #size",
               sync = true)
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
