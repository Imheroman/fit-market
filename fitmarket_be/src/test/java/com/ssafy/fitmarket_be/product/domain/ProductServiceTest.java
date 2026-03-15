package com.ssafy.fitmarket_be.product.domain;

import com.ssafy.fitmarket_be.ai.service.FoodVectorStoreService;
import com.ssafy.fitmarket_be.ai.service.LLMService;
import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.dto.ProductDetailResponse;
import com.ssafy.fitmarket_be.product.dto.ProductListResponse;
import com.ssafy.fitmarket_be.product.dto.ProductUpdateRequest;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import com.ssafy.fitmarket_be.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

/**
 * ProductService 단위 테스트.
 * product.domain 패키지에 위치하여 Product/Nutrition 패키지-private 생성자에 접근한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private LLMService llmService;

    @Mock
    private FoodVectorStoreService foodVectorStoreService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productMapper, llmService, foodVectorStoreService);
    }

    // ===== getProductDetail() =====

    @Test
    @DisplayName("존재하지 않는 상품 ID 로 조회하면 404 예외를 던진다")
    void 존재하지_않는_상품이면_404_예외를_던진다() {
        // Arrange
        Long productId = 999L;
        given(productMapper.selectProductById(productId)).willReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductDetail(productId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("상품을 찾을 수 없습니다");

        verify(productMapper).selectProductById(productId);
    }

    @Test
    @DisplayName("상품 조회 후 카운트를 증가시키고 갱신된 카운트의 상품을 반환한다")
    void 상품_조회_후_카운트를_증가시키고_갱신된_카운트의_상품을_반환한다() {
        // Arrange
        Long productId = 1L;
        Product before = createProduct(productId, 5);   // review_count = 5
        Product after  = createProduct(productId, 6);   // review_count = 6 (증가 후)

        given(productMapper.selectProductById(productId))
            .willReturn(before)   // 1번째 호출: 존재 확인용
            .willReturn(after);   // 2번째 호출: 증가 후 재조회

        // Act
        ProductDetailResponse response = productService.getProductDetail(productId);

        // Assert — 응답에 갱신된 카운트(6) 가 담겨야 한다
        assertThat(response.reviewCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("상품 조회 순서는 selectProductById → incrementReviewCount → selectProductById 다")
    void 상품_조회_순서는_조회_후_카운트_증가_재조회_순서다() {
        // Arrange
        Long productId = 1L;
        Product product = createProduct(productId, 3);
        given(productMapper.selectProductById(productId)).willReturn(product);

        // Act
        productService.getProductDetail(productId);

        // Assert — 호출 순서 검증
        InOrder inOrder = inOrder(productMapper);
        inOrder.verify(productMapper).selectProductById(productId);
        inOrder.verify(productMapper).incrementReviewCount(productId);
        inOrder.verify(productMapper).selectProductById(productId);
    }

    @Test
    @DisplayName("존재하지 않는 상품이면 incrementReviewCount 를 호출하지 않는다")
    void 존재하지_않는_상품이면_카운트를_증가시키지_않는다() {
        // Arrange
        Long productId = 999L;
        given(productMapper.selectProductById(productId)).willReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductDetail(productId))
            .isInstanceOf(ResponseStatusException.class);

        verify(productMapper).selectProductById(productId);
        // incrementReviewCount 는 호출되면 안 된다
        org.mockito.Mockito.verifyNoMoreInteractions(productMapper);
    }

    // ===== getProducts() =====

    @Test
    @DisplayName("getProducts: page·size가 null이면 page=1, size=20으로 기본 페이징이 적용된다")
    void getProducts_기본페이징_page1_size20() {
        // Arrange
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(0L);

        // Act
        PageResponse<ProductListResponse> result = productService.getProducts(null, null, null, null);

        // Assert
        verify(productMapper).selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0));
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(20);
    }

    @Test
    @DisplayName("getProducts: page가 0 이하이면 1로 정규화된다")
    void getProducts_page0이하_1로정규화() {
        // Arrange
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(0L);

        // Act
        PageResponse<ProductListResponse> result = productService.getProducts(0, null, null, null);

        // Assert
        verify(productMapper).selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0));
        assertThat(result.page()).isEqualTo(1);
    }

    @Test
    @DisplayName("getProducts: categoryId가 주어지면 필터로 전달된다")
    void getProducts_카테고리필터_categoryId전달() {
        // Arrange
        given(productMapper.selectProductsWithFilters(eq(2L), isNull(), anyInt(), anyInt()))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(eq(2L), isNull())).willReturn(0L);

        // Act
        productService.getProducts(1, 20, 2L, null);

        // Assert
        verify(productMapper).selectProductsWithFilters(eq(2L), isNull(), eq(20), eq(0));
    }

    @Test
    @DisplayName("getProducts: 공백 keyword는 null로 정규화된다")
    void getProducts_공백키워드_null정규화() {
        // Arrange
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), anyInt(), anyInt()))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(0L);

        // Act
        productService.getProducts(1, 20, null, "   ");

        // Assert
        verify(productMapper).selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0));
    }

    @Test
    @DisplayName("getProducts: totalElements=25, size=10이면 totalPages=3이고 hasNext=true다")
    void getProducts_페이지계산_totalElements25_size10() {
        // Arrange
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(10), eq(0)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(25L);

        // Act
        PageResponse<ProductListResponse> result = productService.getProducts(1, 10, null, null);

        // Assert
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getProducts: 마지막 페이지이면 hasNext=false다")
    void getProducts_마지막페이지_hasNext_false() {
        // Arrange — page=3, totalElements=25, size=10 → offset=20
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(10), eq(20)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(25L);

        // Act
        PageResponse<ProductListResponse> result = productService.getProducts(3, 10, null, null);

        // Assert
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("getProductDetail: selectProductById → incrementReviewCount 순서로 호출된다")
    void getProductDetail_정상_incrementReviewCount호출순서() {
        // Arrange
        Long productId = 1L;
        Product product = createProduct(productId, 3);
        given(productMapper.selectProductById(productId)).willReturn(product);

        // Act
        productService.getProductDetail(productId);

        // Assert
        InOrder inOrder = inOrder(productMapper);
        inOrder.verify(productMapper).selectProductById(productId);
        inOrder.verify(productMapper).incrementReviewCount(productId);
    }

    // ===== updateProduct() =====

    @Test
    @DisplayName("updateProduct: 소유권 불일치이면 FORBIDDEN 예외를 던진다")
    void updateProduct_소유권불일치_FORBIDDEN() {
        // Arrange
        Long productId = 1L;
        Long userId = 2L;
        Product product = createProduct(productId, 0);
        given(productMapper.selectProductById(productId)).willReturn(product);
        given(productMapper.existsByIdAndUserId(productId, userId)).willReturn(false);

        ProductUpdateRequest request = new ProductUpdateRequest(
            "상품명", 1L, 10000L, "설명이 최소 10자 이상", 100, 50, "https://example.com/img.jpg", userId
        );

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(userId, productId, request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("updateProduct: 상품이 없으면 NOT_FOUND 예외를 던진다")
    void updateProduct_상품없음_NOT_FOUND() {
        // Arrange
        Long productId = 999L;
        Long userId = 1L;
        given(productMapper.selectProductById(productId)).willReturn(null);

        ProductUpdateRequest request = new ProductUpdateRequest(
            "상품명", 1L, 10000L, "설명이 최소 10자 이상", 100, 50, "https://example.com/img.jpg", userId
        );

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(userId, productId, request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ===== deleteProduct() =====

    @Test
    @DisplayName("deleteProduct: 소유권 불일치이면 FORBIDDEN 예외를 던진다")
    void deleteProduct_소유권불일치_FORBIDDEN() {
        // Arrange
        Long productId = 1L;
        Long userId = 2L;
        Product product = createProduct(productId, 0);
        given(productMapper.selectProductById(productId)).willReturn(product);
        given(productMapper.existsByIdAndUserId(productId, userId)).willReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(userId, productId))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // ===== getBestProducts() =====

    @Test
    @DisplayName("getBestProducts: page=1, size=12이면 selectBestProducts(12, 0)이 호출된다")
    void getBestProducts_페이징_정상() {
        // Arrange
        given(productMapper.selectBestProducts(eq(12), eq(0))).willReturn(List.of());
        given(productMapper.countProducts()).willReturn(0L);

        // Act
        productService.getBestProducts(1, 12);

        // Assert — offset = (page-1)*size = 0
        verify(productMapper).selectBestProducts(eq(12), eq(0));
    }

    // ===== 테스트 픽스처 =====

    /**
     * product.domain 패키지-private 생성자를 사용해 Product 인스턴스를 생성한다.
     */
    private Product createProduct(Long id, int reviewCount) {
        return new Product(
            id,
            "테스트 상품",
            "상품 설명",
            1L,
            "단백질",
            15000L,
            100,
            "https://example.com/image.jpg",
            4.5,
            reviewCount,
            250,
            25,
            20,
            8
        );
    }
}
