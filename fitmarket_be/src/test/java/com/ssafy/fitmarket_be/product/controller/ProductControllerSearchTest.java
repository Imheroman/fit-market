package com.ssafy.fitmarket_be.product.controller;

import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.dto.ProductListResponse;
import com.ssafy.fitmarket_be.product.service.ProductSearchService;
import com.ssafy.fitmarket_be.product.service.ProductService;
import com.ssafy.fitmarket_be.ranking.service.ProductRankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ProductController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.ssafy\\.fitmarket_be\\.auth\\..*")
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProductController 검색 라우팅")
class ProductControllerSearchTest {

    @MockitoBean
    ProductService productService;

    @MockitoBean
    ProductSearchService productSearchService;

    @MockitoBean
    ProductRankingService rankingService;

    @Autowired
    MockMvc mockMvc;

    private static final PageResponse<ProductListResponse> EMPTY_PAGE =
            new PageResponse<>(List.of(), 1, 20, 0, 0, false, false);

    // ===== 케이스 1: keyword 있으면 ES 검색 라우팅 =====

    @Test
    @DisplayName("keyword가 있으면 productSearchService.search()로 라우팅된다")
    void getProducts_keyword있음_ES검색라우팅() throws Exception {
        // given
        given(productSearchService.search("닭가슴살", null, 1, 10))
                .willReturn(EMPTY_PAGE);

        // when & then
        mockMvc.perform(get("/products")
                        .param("keyword", "닭가슴살")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(productSearchService).search("닭가슴살", null, 1, 10);
        verify(productService, never()).getProducts(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    // ===== 케이스 2: keyword 없으면 MySQL 조회 라우팅 =====

    @Test
    @DisplayName("keyword가 없으면 productService.getProducts()로 라우팅된다")
    void getProducts_keyword없음_MySQL조회라우팅() throws Exception {
        // given
        given(productService.getProducts(1, 20, 1L, null))
                .willReturn(EMPTY_PAGE);

        // when & then
        mockMvc.perform(get("/products")
                        .param("categoryId", "1")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(productService).getProducts(1, 20, 1L, null);
        verify(productSearchService, never()).search(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    // ===== 케이스 3: 파라미터 없으면 기본값으로 MySQL 조회 =====

    @Test
    @DisplayName("파라미터가 없으면 기본값(page=1, size=20)으로 productService.getProducts()가 호출된다")
    void getProducts_파라미터없음_기본값으로MySQL조회() throws Exception {
        // given
        given(productService.getProducts(1, 20, null, null))
                .willReturn(EMPTY_PAGE);

        // when & then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(productService).getProducts(1, 20, null, null);
    }
}
