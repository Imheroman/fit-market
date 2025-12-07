package com.ssafy.fitmarket_be.product.repository;

import com.ssafy.fitmarket_be.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 상품 목록 조회 (페이징).
     */
    List<Product> selectProducts(
        @Param("size") Integer size,
        @Param("offset") Integer offset
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
     * 상품 등록.
     */
    void insertProduct(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("name") String name,
        @Param("description") String description,
        @Param("price") Long price,
        @Param("stock") Integer stock,
        @Param("imageUrl") String imageUrl,
        @Param("foodId") Long foodId
    );

    /**
     * 마지막으로 생성된 ID 조회.
     */
    Long selectLastInsertId();

    /**
     * 판매자의 상품 목록 조회 (email).
     */
    List<Product> selectProductsByEmail(@Param("email") String email);
}
