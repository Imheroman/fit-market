package com.ssafy.fitmarket_be.unit.cart;

import com.ssafy.fitmarket_be.cart.mapper.ShoppingCartMapper;
import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.cart.service.ShoppingCartService;
import com.ssafy.fitmarket_be.ranking.service.ProductRankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppingCartService")
class ShoppingCartServiceTest {

    @Mock
    ShoppingCartRepository shoppingCartRepository;

    @Mock
    ShoppingCartMapper shoppingCartMapper;

    @Mock
    ProductRankingService rankingService;

    @InjectMocks
    ShoppingCartService shoppingCartService;

    // ===== addItem() =====

    @Test
    @DisplayName("addItem: 새 상품 추가 시 insert가 호출되고 true를 반환한다")
    void addItem_새상품추가_true반환() {
        // given
        given(shoppingCartRepository.incrementQuantity(1L, 1L, 1)).willReturn(0);
        given(shoppingCartRepository.insert(1L, 1L, 1)).willReturn(1);

        // when
        boolean result = shoppingCartService.addItem(1L, 1L, 1);

        // then
        verify(shoppingCartRepository).insert(1L, 1L, 1);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("addItem: 기존 상품 수량 증가 시 insert가 호출되지 않고 false를 반환한다")
    void addItem_기존상품수량증가_false반환() {
        // given
        given(shoppingCartRepository.incrementQuantity(1L, 1L, 1)).willReturn(1);

        // when
        boolean result = shoppingCartService.addItem(1L, 1L, 1);

        // then
        verify(shoppingCartRepository, never()).insert(anyLong(), anyLong(), anyInt());
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("addItem: 수량이 0이면 IllegalArgumentException을 던진다")
    void addItem_수량0이하_IllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> shoppingCartService.addItem(1L, 1L, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("수량은 1개 이상부터 담을 수 있어요.");
    }

    @Test
    @DisplayName("addItem: 수량이 음수이면 IllegalArgumentException을 던진다")
    void addItem_수량음수_IllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> shoppingCartService.addItem(1L, 1L, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("수량은 1개 이상부터 담을 수 있어요.");
    }

    @Test
    @DisplayName("addItem: 수량이 100 초과이면 100으로 정규화되어 insert된다")
    void addItem_수량100초과_100으로정규화() {
        // given
        given(shoppingCartRepository.incrementQuantity(eq(1L), eq(1L), eq(100))).willReturn(0);
        given(shoppingCartRepository.insert(eq(1L), eq(1L), eq(100))).willReturn(1);

        // when
        shoppingCartService.addItem(1L, 1L, 150);

        // then — 100으로 정규화되어 insert 호출 검증
        verify(shoppingCartRepository).insert(eq(1L), eq(1L), eq(100));
    }

    @Test
    @DisplayName("addItem: insert 실패 시 IllegalArgumentException을 던진다")
    void addItem_insert실패_IllegalArgumentException() {
        // given
        given(shoppingCartRepository.incrementQuantity(1L, 1L, 1)).willReturn(0);
        given(shoppingCartRepository.insert(1L, 1L, 1)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> shoppingCartService.addItem(1L, 1L, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== updateQuantity() =====

    @Test
    @DisplayName("updateQuantity: 정상 수정 시 예외가 발생하지 않는다")
    void updateQuantity_성공() {
        // given
        given(shoppingCartRepository.updateQuantity(1L, 1L, 5)).willReturn(1);

        // when & then (예외 없이 정상 종료)
        shoppingCartService.updateQuantity(1L, 1L, 5);

        verify(shoppingCartRepository).updateQuantity(1L, 1L, 5);
    }

    @Test
    @DisplayName("updateQuantity: 아이템이 없으면 IllegalArgumentException을 던진다")
    void updateQuantity_아이템없음_IllegalArgumentException() {
        // given
        given(shoppingCartRepository.updateQuantity(1L, 1L, 5)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> shoppingCartService.updateQuantity(1L, 1L, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("수정할 장바구니 상품을 찾을 수 없어요.");
    }

    // ===== delete() =====

    @Test
    @DisplayName("delete: 정상 삭제 시 예외가 발생하지 않는다")
    void delete_성공() {
        // given
        given(shoppingCartRepository.softDelete(1L, 1L)).willReturn(1);

        // when & then (예외 없이 정상 종료)
        shoppingCartService.delete(1L, 1L);

        verify(shoppingCartRepository).softDelete(1L, 1L);
    }

    @Test
    @DisplayName("delete: 아이템이 없으면 IllegalArgumentException을 던진다")
    void delete_아이템없음_IllegalArgumentException() {
        // given
        given(shoppingCartRepository.softDelete(1L, 1L)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> shoppingCartService.delete(1L, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("삭제할 장바구니 상품을 찾을 수 없어요.");
    }

    // ===== countCartItems() =====

    @Test
    @DisplayName("countCartItems: 정상 조회 시 카운트를 반환한다")
    void countCartItems_정상_값반환() {
        // given
        given(shoppingCartRepository.countCartItems(1L)).willReturn(5);

        // when
        int result = shoppingCartService.countCartItems(1L);

        // then
        assertThat(result).isEqualTo(5);
    }
}
