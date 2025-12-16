package com.ssafy.fitmarket_be.seller.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public final class Seller {

  private final Long id;
  private final Long userId;
  private final String businessName;
  private final String businessNumber;
  private final BusinessType businessType;
  private final String contactPhone;
  private final String businessAddress;
  private final String introduction;
  private final SellerStatus status;
  private final String reviewNote;
  private final Long reviewedBy;
  private final LocalDateTime createdDate;
  private final LocalDateTime modifiedDate;
  private final LocalDateTime deletedDate;

  Seller(
      Long id,
      Long userId,
      String businessName,
      String businessNumber,
      BusinessType businessType,
      String contactPhone,
      String businessAddress,
      String introduction,
      SellerStatus status,
      String reviewNote,
      Long reviewedBy,
      LocalDateTime createdDate,
      LocalDateTime modifiedDate,
      LocalDateTime deletedDate
  ) {
    this.id = id;
    this.userId = userId;
    this.businessName = businessName;
    this.businessNumber = businessNumber;
    this.businessType = businessType;
    this.contactPhone = contactPhone;
    this.businessAddress = businessAddress;
    this.introduction = introduction;
    this.status = status == null ? SellerStatus.PENDING : status;
    this.reviewNote = reviewNote;
    this.reviewedBy = reviewedBy;
    this.createdDate = createdDate;
    this.modifiedDate = modifiedDate;
    this.deletedDate = deletedDate;
  }

  public static Seller create(
      Long userId,
      String businessName,
      String businessNumber,
      BusinessType businessType,
      String contactPhone,
      String businessAddress,
      String introduction
  ) {
    return new Seller(
        null,
        userId,
        businessName,
        businessNumber,
        businessType,
        contactPhone,
        businessAddress,
        introduction,
        SellerStatus.PENDING,
        null,
        null,
        null,
        null,
        null
    );
  }

  public boolean isPending() {
    return SellerStatus.PENDING.equals(this.status);
  }

  public Seller reapply(
      String businessName,
      String businessNumber,
      BusinessType businessType,
      String contactPhone,
      String businessAddress,
      String introduction
  ) {
    if (!SellerStatus.REJECTED.equals(this.status)) {
      throw new IllegalStateException("거절된 신청만 재신청할 수 있습니다.");
    }
    return new Seller(
        this.id,
        this.userId,
        businessName,
        businessNumber,
        businessType,
        contactPhone,
        businessAddress,
        introduction,
        SellerStatus.PENDING,
        null,
        null,
        this.createdDate,
        this.modifiedDate,
        this.deletedDate
    );
  }

  public Seller approve(Long reviewerId, String note) {
    return decide(SellerStatus.APPROVED, reviewerId, note);
  }

  public Seller reject(Long reviewerId, String note) {
    if (note == null || note.isBlank()) {
      throw new IllegalArgumentException("거절 시 사유가 필요합니다.");
    }
    return decide(SellerStatus.REJECTED, reviewerId, note);
  }

  public Seller decide(SellerStatus newStatus, Long reviewerId, String note) {
    if (!isPending()) {
      throw new IllegalStateException("이미 처리된 신청입니다.");
    }
    if (newStatus == null) {
      throw new IllegalArgumentException("처리 상태가 필요합니다.");
    }
    return new Seller(
        this.id,
        this.userId,
        this.businessName,
        this.businessNumber,
        this.businessType,
        this.contactPhone,
        this.businessAddress,
        this.introduction,
        newStatus,
        note,
        reviewerId,
        this.createdDate,
        this.modifiedDate,
        this.deletedDate
    );
  }
}
