package com.ssafy.fitmarket_be.product.repository;

import com.ssafy.fitmarket_be.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> selectProducts(
        @Param("size") Integer size,
        @Param("offset") Integer offset
    );

    Long countProducts();
}
