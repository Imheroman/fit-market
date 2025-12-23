package com.ssafy.fitmarket_be.cart.mapper;

import com.ssafy.fitmarket_be.entity.ShoppingCartProduct;
import com.ssafy.fitmarket_be.cart.dto.CartItemResponse;
import com.ssafy.fitmarket_be.cart.dto.CartNutritionResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 장바구니 도메인 모델을 컨트롤러 응답 DTO로 변환하는 매퍼.
 */
@Mapper(componentModel = "spring")
public interface ShoppingCartMapper {

  /**
   * 단일 장바구니 아이템을 응답 DTO로 변환한다.
   *
   * @param shoppingCartProduct 장바구니 도메인 객체
   * @return 응답 DTO
   */
  @Mapping(target = "cartItemId", source = "id")
  @Mapping(target = "nutrition", expression = "java(toNutrition(shoppingCartProduct))")
  CartItemResponse toResponse(ShoppingCartProduct shoppingCartProduct);

  /**
   * 장바구니 아이템 목록을 응답 DTO 목록으로 변환한다.
   *
   * @param shoppingCartProducts 장바구니 도메인 목록
   * @return 응답 DTO 목록
   */
  List<CartItemResponse> toResponseList(List<ShoppingCartProduct> shoppingCartProducts);

  /**
   * 도메인 객체의 영양 필드를 영양 응답 DTO로 변환한다.
   *
   * @param shoppingCartProduct 장바구니 도메인 객체
   * @return 영양 정보 응답 DTO
   */
  default CartNutritionResponse toNutrition(ShoppingCartProduct shoppingCartProduct) {
    return new CartNutritionResponse(
        shoppingCartProduct.getCalories(),
        shoppingCartProduct.getProtein(),
        shoppingCartProduct.getCarbs(),
        shoppingCartProduct.getFat()
    );
  }
}
