package com.ssafy.fitmarket_be.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 상품 등록 요청 DTO.
 * 클라이언트로부터 상품 등록 데이터를 받습니다.
 */
public record ProductCreateRequest(
    @NotBlank(message = "상품명은 필수입니다")
    @Size(min = 3, message = "상품명은 최소 3자 이상이어야 합니다")
    String name,

    @NotNull(message = "카테고리를 선택해주세요")
    @Min(value = 1, message = "올바른 카테고리를 선택해주세요")
    Long categoryId,

    @NotNull(message = "가격은 필수입니다")
    @Min(value = 1000, message = "가격은 1,000원 이상이어야 합니다")
    Long price,

    @NotBlank(message = "상품 설명은 필수입니다")
    @Size(min = 10, message = "상품 설명은 최소 10자 이상이어야 합니다")
    String description,

    @NotNull(message = "상품 중량은 필수입니다")
    @Min(value = 1, message = "상품 중량은 1g 이상이어야 합니다")
    Integer weightG,

    @NotNull(message = "재고는 필수입니다")
    @Min(value = 0, message = "올바른 재고를 입력해주세요")
    Integer stock,

    String imageUrl,

    @NotNull(message = "사용자 ID가 필요합니다")
    Long userId  // TODO: 인증 구현 후 제거 (SecurityContext에서 가져오기)
) {
}