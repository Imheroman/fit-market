package com.ssafy.fitmarket_be.seller.application;

import com.ssafy.fitmarket_be.seller.api.SellerService;
import com.ssafy.fitmarket_be.seller.api.dto.SellerCreateRequest;
import com.ssafy.fitmarket_be.seller.api.dto.SellerResponse;
import com.ssafy.fitmarket_be.seller.api.dto.SellerReviewRequest;
import com.ssafy.fitmarket_be.seller.domain.BusinessType;
import com.ssafy.fitmarket_be.seller.domain.Seller;
import com.ssafy.fitmarket_be.seller.domain.SellerStatus;
import com.ssafy.fitmarket_be.seller.infrastructure.mybatis.SellerMapper;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ssafy.fitmarket_be.entity.User;

@Service
@RequiredArgsConstructor
class SellerServiceImpl implements SellerService {

  private final SellerMapper sellerMapper;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public SellerResponse apply(Long userId, SellerCreateRequest request) {
    var existingOpt = sellerMapper.findActiveByUserId(userId);
    if (existingOpt.isPresent()) {
      Seller existing = existingOpt.get();
      if (!SellerStatus.REJECTED.equals(existing.getStatus())) {
        throw new IllegalStateException("이미 접수된 판매자 신청이 있습니다.");
      }
      Seller reapply = existing.reapply(
          request.businessName(),
          request.businessNumber(),
          BusinessType.from(request.businessType()),
          request.contactPhone(),
          request.businessAddress(),
          request.introduction()
      );

      int updated = sellerMapper.updateForReapply(reapply);
      if (updated <= 0) {
        throw new IllegalStateException("판매자 재신청을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.");
      }

      Seller refreshed = sellerMapper.findActiveById(existing.getId())
          .orElseThrow(() -> new IllegalStateException("재신청 정보 조회에 실패했습니다."));
      User user = userRepository.findBy(userId)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
      return SellerResponse.from(refreshed, user.getName(), user.getEmail());
    }

    Seller seller = Seller.create(
        userId,
        request.businessName(),
        request.businessNumber(),
        BusinessType.from(request.businessType()),
        request.contactPhone(),
        request.businessAddress(),
        request.introduction()
    );

    int inserted = sellerMapper.insert(seller);
    if (inserted <= 0) {
      throw new IllegalStateException("판매자 신청을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.");
    }

    User user = userRepository.findBy(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    return SellerResponse.from(seller, user.getName(), user.getEmail());
  }

  @Override
  @Transactional(readOnly = true)
  public SellerResponse getMyApplication(Long userId) {
    Seller seller = sellerMapper.findActiveByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("등록된 판매자 신청을 찾을 수 없습니다."));
    User user = userRepository.findBy(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    return SellerResponse.from(seller, user.getName(), user.getEmail());
  }

  @Override
  @Transactional(readOnly = true)
  public List<SellerResponse> listByStatus(String status) {
    SellerStatus normalized = normalizeStatusOrDefault(status);
    return sellerMapper.findByStatus(normalized).stream()
        .map(seller -> {
          User user = userRepository.findBy(seller.getUserId())
              .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
          return SellerResponse.from(seller, user.getName(), user.getEmail());
        })
        .toList();
  }

  @Override
  @Transactional
  public SellerResponse review(Long sellerId, Long reviewerId,
      SellerReviewRequest request) {
    Seller seller = sellerMapper.findActiveById(sellerId)
        .orElseThrow(() -> new IllegalArgumentException("판매자 신청을 찾을 수 없습니다."));

    SellerStatus decision = SellerStatus.from(request.decision());
    Seller decided = switch (decision) {
      case APPROVED -> seller.approve(reviewerId, request.reviewNote());
      case REJECTED -> seller.reject(reviewerId, request.reviewNote());
      case PENDING -> throw new IllegalArgumentException("처리 상태는 승인 또는 거절만 가능합니다.");
    };

    int updated = sellerMapper.updateStatus(
        sellerId,
        decided.getStatus(),
        decided.getReviewNote(),
        decided.getReviewedBy()
    );

    if (updated <= 0) {
      throw new IllegalStateException("판매자 신청 상태를 변경하지 못했습니다.");
    }

    if (SellerStatus.APPROVED.equals(decision)) {
      int roleUpdated = userRepository.updateRole(seller.getUserId(), "SELLER");
      if (roleUpdated <= 0) {
        throw new IllegalStateException("사용자 권한을 판매자로 변경하지 못했습니다.");
      }
    }

    Seller refreshed = sellerMapper.findActiveById(sellerId)
        .orElseThrow(() -> new IllegalStateException("처리 후 신청 정보를 조회하지 못했습니다."));

    User user = userRepository.findBy(refreshed.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    return SellerResponse.from(refreshed, user.getName(), user.getEmail());
  }

  private SellerStatus normalizeStatusOrDefault(String raw) {
    if (raw == null || raw.isBlank()) {
      return SellerStatus.PENDING;
    }
    return SellerStatus.from(raw);
  }
}
