package com.ssafy.fitmarket_be.seller.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerReviewRequest(
    @NotBlank(message = "승인 또는 거절을 선택해 주세요.")
    String decision, // approved / rejected

    @Size(max = 255, message = "메모는 255자 이내로 입력해 주세요.")
    String reviewNote
) {
}
