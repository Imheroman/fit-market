package com.ssafy.fitmarket_be.seller.api.dto;

import com.ssafy.fitmarket_be.seller.domain.Seller;
import java.time.LocalDateTime;

public record SellerResponse(
    Long id,
    Long userId,
    String userName,
    String userEmail,
    String businessName,
    String businessNumber,
    String businessType,
    String contactPhone,
    String businessAddress,
    String introduction,
    String status,
    String reviewNote,
    Long reviewedBy,
    LocalDateTime createdDate,
    LocalDateTime modifiedDate
) {
  public static SellerResponse from(Seller seller, String userName, String userEmail) {
    return new SellerResponse(
        seller.getId(),
        seller.getUserId(),
        userName,
        userEmail,
        seller.getBusinessName(),
        seller.getBusinessNumber(),
        seller.getBusinessType().getCode(),
        seller.getContactPhone(),
        seller.getBusinessAddress(),
        seller.getIntroduction(),
        seller.getStatus().getCode(),
        seller.getReviewNote(),
        seller.getReviewedBy(),
        seller.getCreatedDate(),
        seller.getModifiedDate()
    );
  }
}
