package com.ssafy.fitmarket_be.seller.presentation;

import com.ssafy.fitmarket_be.seller.api.SellerService;
import com.ssafy.fitmarket_be.seller.api.dto.SellerCreateRequest;
import com.ssafy.fitmarket_be.seller.api.dto.SellerResponse;
import com.ssafy.fitmarket_be.seller.api.dto.SellerReviewRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<SellerResponse> apply(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @Valid @RequestBody SellerCreateRequest request
  ) {
    SellerResponse response = sellerService.apply(userId, request);
    return ResponseEntity
        .created(URI.create("/seller/" + response.id()))
        .body(response);
  }

  @GetMapping("/me")
  public ResponseEntity<SellerResponse> getMyApplication(
      @AuthenticationPrincipal(expression = "id") Long userId
  ) {
    SellerResponse response = sellerService.getMyApplication(userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<SellerResponse>> list(
      @RequestParam(name = "status", required = false) String status
  ) {
    List<SellerResponse> response = sellerService.listByStatus(status);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/review")
  public ResponseEntity<SellerResponse> review(
      @PathVariable("id") Long sellerId,
      @AuthenticationPrincipal(expression = "id") Long reviewerId,
      @Valid @RequestBody SellerReviewRequest request
  ) {
    SellerResponse response = sellerService.review(sellerId, reviewerId, request);
    return ResponseEntity.ok(response);
  }
}
