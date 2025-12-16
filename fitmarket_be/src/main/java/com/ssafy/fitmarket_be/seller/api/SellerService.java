package com.ssafy.fitmarket_be.seller.api;

import com.ssafy.fitmarket_be.seller.api.dto.SellerCreateRequest;
import com.ssafy.fitmarket_be.seller.api.dto.SellerResponse;
import com.ssafy.fitmarket_be.seller.api.dto.SellerReviewRequest;
import java.util.List;

public interface SellerService {
  SellerResponse apply(Long userId, SellerCreateRequest request);

  SellerResponse getMyApplication(Long userId);

  List<SellerResponse> listByStatus(String status);

  SellerResponse review(Long applicationId, Long reviewerId, SellerReviewRequest request);
}
