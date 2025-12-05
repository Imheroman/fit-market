package com.ssafy.fitmarket_be.product.controller;

import com.ssafy.fitmarket_be.global.dto.PageResponse;
import com.ssafy.fitmarket_be.product.dto.ProductResponse;
import com.ssafy.fitmarket_be.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<ProductResponse> response = productService.getProducts(page, size);
        return ResponseEntity.ok(response);
    }
}
