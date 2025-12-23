package com.ssafy.fitmarket_be.seller.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerCreateRequest(
    @NotBlank(message = "상호명을 입력해 주세요.")
    @Size(max = 100, message = "상호명은 100자 이내로 입력해 주세요.")
    String businessName,

    @NotBlank(message = "사업자 등록번호를 입력해 주세요.")
    @Size(max = 50, message = "사업자 등록번호는 50자 이내로 입력해 주세요.")
    String businessNumber,

    @NotBlank(message = "사업자 유형을 선택해 주세요.")
    @Size(max = 20, message = "사업자 유형은 20자 이내로 입력해 주세요.")
    String businessType,

    @NotBlank(message = "대표 연락처를 입력해 주세요.")
    @Size(max = 30, message = "대표 연락처는 30자 이내로 입력해 주세요.")
    String contactPhone,

    @NotBlank(message = "사업장 주소를 입력해 주세요.")
    @Size(max = 255, message = "사업장 주소는 255자 이내로 입력해 주세요.")
    String businessAddress,

    @NotBlank(message = "사업 소개를 입력해 주세요.")
    @Size(min = 20, max = 500, message = "사업 소개는 20~500자 이내로 입력해 주세요.")
    String introduction
) {
}
