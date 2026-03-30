package com.ssafy.fitmarket_be.product.domain;

/**
 * ProductService 단위 테스트용 픽스처.
 * Product의 package-private 생성자에 접근하기 위해 동일 패키지에 위치합니다.
 */
public class ProductFixture {

    public static Product create(Long id, int reviewCount) {
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
