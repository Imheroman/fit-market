package com.ssafy.fitmarket_be.product.controller;

import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.dto.ProductCreateRequest;
import com.ssafy.fitmarket_be.product.dto.ProductResponse;
import com.ssafy.fitmarket_be.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회 (페이징).
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<ProductResponse> response = productService.getProducts(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 등록.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.createProduct(request);

        return ResponseEntity
            .created(URI.create("/products/" + response.id()))
            .body(response);
    }
}
