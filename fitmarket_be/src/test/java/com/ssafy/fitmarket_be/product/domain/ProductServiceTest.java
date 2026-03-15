package com.ssafy.fitmarket_be.product.domain;

import com.ssafy.fitmarket_be.ai.service.FoodVectorStoreService;
import com.ssafy.fitmarket_be.ai.service.LLMService;
import com.ssafy.fitmarket_be.product.dto.ProductDetailResponse;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import com.ssafy.fitmarket_be.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
