package com.ssafy.fitmarket_be.cart.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.fitmarket_be.cart.repository.ShoppingCartRepository;
import com.ssafy.fitmarket_be.entity.ShoppingCartProduct;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DisplayName("ShoppingCartRepository — SQL 정합성")
class ShoppingCartRepositoryTest {

    @Autowired
    ShoppingCartRepository shoppingCartRepository;

    @Test
    @DisplayName("insert_신규_장바구니아이템추가")
    void insert_신규_장바구니아이템추가() {
        // given — test-data: userId=1, productId=1 이미 존재하므로 productId=2 사용

        // when
        int result = shoppingCartRepository.insert(2L, 1L, 1);

        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("incrementQuantity_기존아이템_수량증가")
    void incrementQuantity_기존아이템_수량증가() {
        // given — test-data: userId=1, productId=1 이미 존재 (qty=2)

        // when
        int result = shoppingCartRepository.incrementQuantity(1L, 1L, 2);

        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("softDelete_성공_활성아이템제외됨")
    void softDelete_성공_활성아이템제외됨() {
        // given — test-data: cartItemId=1, userId=1 존재 (active)
        int beforeCount = shoppingCartRepository.countCartItems(1L);
        assertThat(beforeCount).isGreaterThanOrEqualTo(1);

        // when
        int deleted = shoppingCartRepository.softDelete(1L, 1L);

        // then
        assertThat(deleted).isEqualTo(1);
        int afterCount = shoppingCartRepository.countCartItems(1L);
        assertThat(afterCount).isEqualTo(beforeCount - 1);
    }

    @Test
    @DisplayName("countCartItems_삭제제외_카운트")
    void countCartItems_삭제제외_카운트() {
        // given — test-data: userId=1에 2개 활성 아이템 존재

        // when
        int count = shoppingCartRepository.countCartItems(1L);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("findByIds_ids일치_반환")
    void findByIds_ids일치_반환() {
        // given — test-data: cartItemId=1, userId=1 존재

        // when
        List<ShoppingCartProduct> items = shoppingCartRepository.findByIds(1L, List.of(1L));

        // then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(1L);
    }
}
