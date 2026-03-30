package com.ssafy.fitmarket_be.seller.presentation;

import com.ssafy.fitmarket_be.global.common.ApiResponse;
import com.ssafy.fitmarket_be.seller.api.SellerService;
import com.ssafy.fitmarket_be.seller.api.dto.SellerCreateRequest;
import com.ssafy.fitmarket_be.seller.api.dto.SellerResponse;
import com.ssafy.fitmarket_be.seller.api.dto.SellerReviewRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller")
public class SellerController {

  private final SellerService sellerService;

  @PostMapping
  public ResponseEntity<ApiResponse<SellerResponse>> apply(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @Valid @RequestBody SellerCreateRequest request
  ) {
    SellerResponse response = sellerService.apply(userId, request);
    return ResponseEntity
        .created(URI.create("/seller/" + response.id()))
        .body(ApiResponse.success(response));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<SellerResponse>> getMyApplication(
      @AuthenticationPrincipal(expression = "id") Long userId
  ) {
    SellerResponse response = sellerService.getMyApplication(userId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<SellerResponse>>> list(
      @RequestParam(name = "status", required = false) String status
  ) {
    List<SellerResponse> response = sellerService.listByStatus(status);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/{id}/review")
  public ResponseEntity<ApiResponse<SellerResponse>> review(
      @PathVariable("id") Long sellerId,
      @AuthenticationPrincipal(expression = "id") Long reviewerId,
      @Valid @RequestBody SellerReviewRequest request
  ) {
    SellerResponse response = sellerService.review(sellerId, reviewerId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
