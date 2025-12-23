package com.ssafy.fitmarket_be.product.repository;

import com.ssafy.fitmarket_be.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 상품 목록 조회 (필터링, 페이징).
     * categoryId와 keyword를 동적으로 적용합니다.
     */
    List<Product> selectProductsWithFilters(
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword,
        @Param("size") Integer size,
        @Param("offset") Integer offset
    );

    /**
     * 상품 개수 조회 (필터링).
     * categoryId와 keyword를 동적으로 적용합니다.
     */
    Long countProductsWithFilters(
        @Param("categoryId") Long categoryId,
        @Param("keyword") String keyword
    );

    /**
     * 전체 상품 개수 조회.
     */
    Long countProducts();

    /**
     * 상품 ID로 조회.
     */
    Product selectProductById(@Param("id") Long id);

    /**
     * review_count 증가.
     */
    void incrementReviewCount(@Param("productId") Long productId);

    /**
     * 베스트 상품 조회 (페이징).
     */
    List<Product> selectBestProducts(
        @Param("size") Integer size,
        @Param("offset") Integer offset
    );

    /**
     * 신상품 조회 (페이징).
     */
    List<Product> selectNewProducts(
        @Param("size") Integer size,
        @Param("offset") Integer offset
    );

    /**
     * 상품 등록.
     */
    void insertProduct(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("name") String name,
        @Param("description") String description,
        @Param("price") Long price,
        @Param("weightG") Integer weightG,
        @Param("stock") Integer stock,
        @Param("imageUrl") String imageUrl,
        @Param("foodId") Long foodId
    );

    /**
     * 마지막으로 생성된 ID 조회.
     */
    Long selectLastInsertId();

    /**
     * 상품 수정.
     */
    void updateProduct(
        @Param("productId") Long productId,
        @Param("name") String name,
        @Param("categoryId") Long categoryId,
        @Param("price") Long price,
        @Param("description") String description,
        @Param("weightG") Integer weightG,
        @Param("stock") Integer stock,
        @Param("imageUrl") String imageUrl
    );

    /**
     * 상품 삭제 (소프트 삭제).
     */
    void deleteProduct(@Param("productId") Long productId);

    /**
     * 판매자의 상품 목록 조회 (userId).
     */
    List<Product> selectProductsByUserId(@Param("userId") Long userId);

    /**
     * 베스트 상품 조회 (평점/리뷰순, 제한 개수).
     */
    List<Product> selectBestProducts(@Param("limit") Integer limit);

    /**
     * 신상품 조회 (최신순, 제한 개수).
     */
    List<Product> selectNewProducts(@Param("limit") Integer limit);
}
