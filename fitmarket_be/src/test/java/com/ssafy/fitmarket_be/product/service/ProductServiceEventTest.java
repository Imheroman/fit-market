package com.ssafy.fitmarket_be.product.service;

import com.ssafy.fitmarket_be.ai.service.FoodVectorStoreService;
import com.ssafy.fitmarket_be.ai.service.LLMService;
import com.ssafy.fitmarket_be.product.domain.Product;
import com.ssafy.fitmarket_be.product.domain.ProductFixture;
import com.ssafy.fitmarket_be.product.dto.ProductCreateRequest;
import com.ssafy.fitmarket_be.product.dto.ProductUpdateRequest;
import com.ssafy.fitmarket_be.product.event.ProductEvent;
import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 이벤트 발행")
class ProductServiceEventTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private LLMService llmService;

    @Mock
    private FoodVectorStoreService foodVectorStoreService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productMapper, llmService, foodVectorStoreService, eventPublisher);
        ReflectionTestUtils.setField(productService, "aiEnabled", false);
    }

    @Test
    @DisplayName("createProduct 성공 시 ProductEvent.Created 이벤트를 발행한다")
    void createProduct_성공시_CreatedEvent발행() {
        // given
        Long userId = 1L;
        ProductCreateRequest request = new ProductCreateRequest(
                "테스트 상품명", 1L, 15000L, "상품 설명 최소 10자 이상입니다", 250, 100, "https://example.com/image.jpg", userId
        );
        given(productMapper.selectLastInsertId()).willReturn(10L);
        given(productMapper.selectProductById(10L)).willReturn(ProductFixture.create(10L, 0));

        // when
        productService.createProduct(userId, request);

        // then
        verify(productMapper).insertProduct(eq(userId), anyLong(), anyString(), anyString(), anyLong(), anyInt(), anyInt(), anyString(), any());
        verify(eventPublisher).publishEvent(isA(ProductEvent.Created.class));
    }

    @Test
    @DisplayName("updateProduct 성공 시 ProductEvent.Updated 이벤트를 발행한다")
    void updateProduct_성공시_UpdatedEvent발행() {
        // given
        Long userId = 1L;
        Long productId = 10L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "수정된 상품명", 1L, 20000L, "수정된 설명 최소 10자 이상입니다", 300, 50, "https://example.com/updated.jpg", userId
        );
        given(productMapper.selectProductById(productId)).willReturn(ProductFixture.create(productId, 5));
        given(productMapper.existsByIdAndUserId(productId, userId)).willReturn(true);

        // when
        productService.updateProduct(userId, productId, request);

        // then
        verify(eventPublisher).publishEvent(isA(ProductEvent.Updated.class));
    }

    @Test
    @DisplayName("deleteProduct 성공 시 ProductEvent.Deleted 이벤트를 발행한다")
    void deleteProduct_성공시_DeletedEvent발행() {
        // given
        Long userId = 1L;
        Long productId = 10L;
        given(productMapper.selectProductById(productId)).willReturn(ProductFixture.create(productId, 0));
        given(productMapper.existsByIdAndUserId(productId, userId)).willReturn(true);

        // when
        productService.deleteProduct(userId, productId);

        // then
        InOrder inOrder = inOrder(productMapper, eventPublisher);
        inOrder.verify(productMapper).deleteProduct(productId);
        inOrder.verify(eventPublisher).publishEvent(isA(ProductEvent.Deleted.class));
    }
}
