package com.ssafy.fitmarket_be.product.event;

/**
 * 상품 CUD 이벤트 -- ES 동기화용.
 * sealed interface + record로 패턴 매칭 지원.
 */
public sealed interface ProductEvent {
    Long productId();

    record Created(Long productId) implements ProductEvent {}
    record Updated(Long productId) implements ProductEvent {}
    record Deleted(Long productId) implements ProductEvent {}
}
