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
        // userId=1, productId=1은 이미 test-data에 존재하지만 새로운 row를 INSERT
        // productId=2 사용
        int result = shoppingCartRepository.insert(2L, 1L, 1);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("incrementQuantity_기존아이템_수량증가")
    void incrementQuantity_기존아이템_수량증가() {
        // test-data: userId=1, productId=1 이미 존재 (qty=2)
        int result = shoppingCartRepository.incrementQuantity(1L, 1L, 2);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("softDelete_성공_활성아이템제외됨")
    void softDelete_성공_활성아이템제외됨() {
        // test-data: cartItemId=1, userId=1 존재 (active)
        // 소프트 삭제 후 countCartItems에서 제외되는지 확인
        int beforeCount = shoppingCartRepository.countCartItems(1L);
        assertThat(beforeCount).isGreaterThanOrEqualTo(1);

        int deleted = shoppingCartRepository.softDelete(1L, 1L);
        assertThat(deleted).isEqualTo(1);

        int afterCount = shoppingCartRepository.countCartItems(1L);
        assertThat(afterCount).isEqualTo(beforeCount - 1);
    }

    @Test
    @DisplayName("countCartItems_삭제제외_카운트")
    void countCartItems_삭제제외_카운트() {
        // test-data: userId=1에 2개 활성 아이템 존재
        int count = shoppingCartRepository.countCartItems(1L);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("findByIds_ids일치_반환")
    void findByIds_ids일치_반환() {
        // test-data: cartItemId=1, userId=1 존재
        List<ShoppingCartProduct> items = shoppingCartRepository.findByIds(1L, List.of(1L));

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(1L);
    }
}
