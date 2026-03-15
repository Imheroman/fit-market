package com.ssafy.fitmarket_be.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderApprovalStatus")
class OrderApprovalStatusTest {

    // ===== isTerminal() =====

    @Test
    @DisplayName("CANCELLED 는 terminal 상태다")
    void CANCELLED_는_terminal_상태다() {
        assertThat(OrderApprovalStatus.CANCELLED.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("REJECTED 는 terminal 상태다")
    void REJECTED_는_terminal_상태다() {
        assertThat(OrderApprovalStatus.REJECTED.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("DELIVERED 는 terminal 상태가 아니다 — 반품/교환이 가능하기 때문")
    void DELIVERED_는_terminal_상태가_아니다() {
        // DELIVERED 는 반품/교환 플로우가 열려 있으므로 terminal 이 아니어야 한다
        assertThat(OrderApprovalStatus.DELIVERED.isTerminal()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = OrderApprovalStatus.class, names = {"PENDING_APPROVAL", "APPROVED", "SHIPPING", "DELIVERED"})
    @DisplayName("취소/거절 외 상태는 terminal 이 아니다")
    void 취소거절_외_상태는_terminal이_아니다(OrderApprovalStatus status) {
        assertThat(status.isTerminal()).isFalse();
    }

    // ===== isShippingOrLater() =====

    @Test
    @DisplayName("SHIPPING 은 배송 시작 이후 상태다")
    void SHIPPING_은_배송시작_이후_상태다() {
        assertThat(OrderApprovalStatus.SHIPPING.isShippingOrLater()).isTrue();
    }

    @Test
    @DisplayName("DELIVERED 는 배송 시작 이후 상태다")
    void DELIVERED_는_배송시작_이후_상태다() {
        assertThat(OrderApprovalStatus.DELIVERED.isShippingOrLater()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = OrderApprovalStatus.class,
        names = {"PENDING_APPROVAL", "APPROVED", "CANCELLED", "REJECTED"})
    @DisplayName("배송 전 상태는 isShippingOrLater 가 false 다")
    void 배송전_상태는_isShippingOrLater가_false다(OrderApprovalStatus status) {
        assertThat(status.isShippingOrLater()).isFalse();
    }

    // ===== from() =====

    @Test
    @DisplayName("유효한 dbValue 로 변환할 수 있다")
    void 유효한_dbValue로_변환할_수_있다() {
        assertThat(OrderApprovalStatus.from("pending_approval"))
            .isEqualTo(OrderApprovalStatus.PENDING_APPROVAL);
        assertThat(OrderApprovalStatus.from("approved"))
            .isEqualTo(OrderApprovalStatus.APPROVED);
        assertThat(OrderApprovalStatus.from("delivered"))
            .isEqualTo(OrderApprovalStatus.DELIVERED);
    }

    @Test
    @DisplayName("대소문자 구분 없이 변환된다")
    void 대소문자_구분없이_변환된다() {
        assertThat(OrderApprovalStatus.from("DELIVERED"))
            .isEqualTo(OrderApprovalStatus.DELIVERED);
        assertThat(OrderApprovalStatus.from("Shipping"))
            .isEqualTo(OrderApprovalStatus.SHIPPING);
    }

    @Test
    @DisplayName("지원하지 않는 값이면 IllegalArgumentException 을 던진다")
    void 지원하지_않는_값이면_예외를_던진다() {
        assertThatThrownBy(() -> OrderApprovalStatus.from("unknown"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
