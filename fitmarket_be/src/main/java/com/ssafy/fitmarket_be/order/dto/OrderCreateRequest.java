package com.ssafy.fitmarket_be.order.dto;

import com.ssafy.fitmarket_be.order.domain.OrderMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 주문 생성 요청 DTO.
 *
 * @param orderNumber   외부에서 미리 생성한 주문 번호(선택)
 * @param mode          주문 모드(cart/direct)
 * @param productId     바로구매 시 상품 식별자
 * @param quantity      바로구매 시 수량
 * @param cartItemIds   장바구니 주문 시 선택한 장바구니 아이디 목록
 * @param addressId     배송지 식별자
 * @param shippingFee   배송비(없으면 0)
 * @param discountAmount 할인 금액(없으면 0)
 * @param comment       주문 메모
 */
public record OrderCreateRequest(
    @Size(max = 40, message = "주문 번호가 너무 길어요. 다시 시도해 주세요.")
    String orderNumber,
    @NotNull String mode,
    Long productId,
    @Positive(message = "구매 수량은 1개 이상이어야 해요.")
    @Max(value = 100, message = "한 상품은 한 번에 최대 100개까지 주문할 수 있어요.") Integer quantity,
    List<Long> cartItemIds,
    @NotNull(message = "배송지를 선택해 주세요.") Long addressId,
    @PositiveOrZero(message = "배송비는 음수가 될 수 없어요.") Long shippingFee,
    @PositiveOrZero(message = "할인 금액은 음수가 될 수 없어요.") Long discountAmount,
    String comment
) {

  /**
   * 문자열 모드를 enum으로 변환한다.
   *
   * @return 주문 모드 enum
   */
  public OrderMode resolvedMode() {
    return OrderMode.from(mode);
  }
}
