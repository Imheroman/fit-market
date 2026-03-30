package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.ai.service.FoodVectorStoreService;
import com.ssafy.fitmarket_be.ai.service.LLMService;
import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.domain.ProductFixture;
import com.ssafy.fitmarket_be.product.dto.ProductDetailResponse;
import com.ssafy.fitmarket_be.product.dto.ProductListResponse;
import com.ssafy.fitmarket_be.product.dto.ProductUpdateRequest;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private LLMService llmService;

    @Mock
    private FoodVectorStoreService foodVectorStoreService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productMapper, llmService, foodVectorStoreService, eventPublisher);
    }

    // ===== getProductDetail() =====

    @Test
    @DisplayName("존재하지 않는 상품 ID 로 조회하면 404 예외를 던진다")
    void 존재하지_않는_상품이면_404_예외를_던진다() {
        // given
        Long productId = 999L;
        given(productMapper.selectProductById(productId)).willReturn(null);

        // when / then
        assertThatThrownBy(() -> productService.getProductDetail(productId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("상품을 찾을 수 없습니다");

        verify(productMapper).selectProductById(productId);
    }

    @Test
    @DisplayName("상품 조회 후 카운트를 증가시키고 갱신된 카운트의 상품을 반환한다")
    void 상품_조회_후_카운트를_증가시키고_갱신된_카운트의_상품을_반환한다() {
        // given
        Long productId = 1L;
        Product before = ProductFixture.create(productId, 5);
        Product after  = ProductFixture.create(productId, 6);

        given(productMapper.selectProductById(productId))
            .willReturn(before)
            .willReturn(after);

        // when
        ProductDetailResponse response = productService.getProductDetail(productId);

        // then
        assertThat(response.reviewCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("상품 조회 순서는 selectProductById → incrementViewCount → selectProductById 다")
    void 상품_조회_순서는_조회_후_카운트_증가_재조회_순서다() {
        // given
        Long productId = 1L;
        Product product = ProductFixture.create(productId, 3);
        given(productMapper.selectProductById(productId)).willReturn(product);

        // when
        productService.getProductDetail(productId);

        // then
        InOrder inOrder = inOrder(productMapper);
        inOrder.verify(productMapper).selectProductById(productId);
        inOrder.verify(productMapper).incrementViewCount(productId);
        inOrder.verify(productMapper).selectProductById(productId);
    }

    @Test
    @DisplayName("존재하지 않는 상품이면 incrementViewCount 를 호출하지 않는다")
    void 존재하지_않는_상품이면_카운트를_증가시키지_않는다() {
        // given
        Long productId = 999L;
        given(productMapper.selectProductById(productId)).willReturn(null);

        // when / then
        assertThatThrownBy(() -> productService.getProductDetail(productId))
            .isInstanceOf(ResponseStatusException.class);

        verify(productMapper).selectProductById(productId);
        org.mockito.Mockito.verifyNoMoreInteractions(productMapper);
    }

    // ===== getProducts() =====

    @Test
    @DisplayName("getProducts: page·size가 null이면 page=1, size=20으로 기본 페이징이 적용된다")
    void getProducts_기본페이징_page1_size20() {
        // given
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(0L);

        // when
        PageResponse<ProductListResponse> result = productService.getProducts(null, null, null, null);

        // then
        verify(productMapper).selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0));
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(20);
    }

    @Test
    @DisplayName("getProducts: page가 0 이하이면 1로 정규화된다")
    void getProducts_page0이하_1로정규화() {
        // given
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(0L);

        // when
        PageResponse<ProductListResponse> result = productService.getProducts(0, null, null, null);

        // then
        verify(productMapper).selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0));
        assertThat(result.page()).isEqualTo(1);
    }

    @Test
    @DisplayName("getProducts: categoryId가 주어지면 필터로 전달된다")
    void getProducts_카테고리필터_categoryId전달() {
        // given
        given(productMapper.selectProductsWithFilters(eq(2L), isNull(), anyInt(), anyInt()))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(eq(2L), isNull())).willReturn(0L);

        // when
        productService.getProducts(1, 20, 2L, null);

        // then
        verify(productMapper).selectProductsWithFilters(eq(2L), isNull(), eq(20), eq(0));
    }

    @Test
    @DisplayName("getProducts: 공백 keyword는 null로 정규화된다")
    void getProducts_공백키워드_null정규화() {
        // given
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), anyInt(), anyInt()))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(0L);

        // when
        productService.getProducts(1, 20, null, "   ");

        // then
        verify(productMapper).selectProductsWithFilters(isNull(), isNull(), eq(20), eq(0));
    }

    @Test
    @DisplayName("getProducts: totalElements=25, size=10이면 totalPages=3이고 hasNext=true다")
    void getProducts_페이지계산_totalElements25_size10() {
        // given
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(10), eq(0)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(25L);

        // when
        PageResponse<ProductListResponse> result = productService.getProducts(1, 10, null, null);

        // then
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getProducts: 마지막 페이지이면 hasNext=false다")
    void getProducts_마지막페이지_hasNext_false() {
        // given — page=3, totalElements=25, size=10 → offset=20
        given(productMapper.selectProductsWithFilters(isNull(), isNull(), eq(10), eq(20)))
            .willReturn(List.of());
        given(productMapper.countProductsWithFilters(isNull(), isNull())).willReturn(25L);

        // when
        PageResponse<ProductListResponse> result = productService.getProducts(3, 10, null, null);

        // then
        assertThat(result.hasNext()).isFalse();
    }

    // ===== updateProduct() =====

    @Test
    @DisplayName("updateProduct: 소유권 불일치이면 FORBIDDEN 예외를 던진다")
    void updateProduct_소유권불일치_FORBIDDEN() {
        // given
        Long productId = 1L;
        Long userId = 2L;
        Product product = ProductFixture.create(productId, 0);
        given(productMapper.selectProductById(productId)).willReturn(product);
        given(productMapper.existsByIdAndUserId(productId, userId)).willReturn(false);

        ProductUpdateRequest request = new ProductUpdateRequest(
            "상품명", 1L, 10000L, "설명이 최소 10자 이상", 100, 50, "https://example.com/img.jpg", userId
        );

        // when / then
        assertThatThrownBy(() -> productService.updateProduct(userId, productId, request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("updateProduct: 상품이 없으면 NOT_FOUND 예외를 던진다")
    void updateProduct_상품없음_NOT_FOUND() {
        // given
        Long productId = 999L;
        Long userId = 1L;
        given(productMapper.selectProductById(productId)).willReturn(null);

        ProductUpdateRequest request = new ProductUpdateRequest(
            "상품명", 1L, 10000L, "설명이 최소 10자 이상", 100, 50, "https://example.com/img.jpg", userId
        );

        // when / then
        assertThatThrownBy(() -> productService.updateProduct(userId, productId, request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ===== deleteProduct() =====

    @Test
    @DisplayName("deleteProduct: 소유권 불일치이면 FORBIDDEN 예외를 던진다")
    void deleteProduct_소유권불일치_FORBIDDEN() {
        // given
        Long productId = 1L;
        Long userId = 2L;
        Product product = ProductFixture.create(productId, 0);
        given(productMapper.selectProductById(productId)).willReturn(product);
        given(productMapper.existsByIdAndUserId(productId, userId)).willReturn(false);

        // when / then
        assertThatThrownBy(() -> productService.deleteProduct(userId, productId))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // ===== getBestProducts() =====

    @Test
    @DisplayName("getBestProducts: page=1, size=12이면 selectBestProducts(12, 0)이 호출된다")
    void getBestProducts_페이징_정상() {
        // given
        given(productMapper.selectBestProducts(eq(12), eq(0))).willReturn(List.of());
        given(productMapper.countProducts()).willReturn(0L);

        // when
        productService.getBestProducts(1, 12);

        // then
        verify(productMapper).selectBestProducts(eq(12), eq(0));
    }
}
