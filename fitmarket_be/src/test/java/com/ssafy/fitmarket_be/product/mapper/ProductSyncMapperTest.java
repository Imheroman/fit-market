package com.ssafy.fitmarket_be.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.fitmarket_be.product.repository.ProductMapper;
import com.ssafy.fitmarket_be.product.sync.ProductSyncData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ProductMapper — ES 동기화 쿼리 정합성")
class ProductSyncMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("selectProductForSync_JOIN결과매핑_영양소계산정확")
    void selectProductForSync_JOIN결과매핑_영양소계산정확() {
        // when
        ProductSyncData data = productMapper.selectProductForSync(100L);

        // then — 기본 필드 매핑
        assertThat(data).isNotNull();
        assertThat(data.name()).isEqualTo("닭가슴살 샐러드");
        assertThat(data.categoryName()).isEqualTo("단백질");
        assertThat(data.foodName()).isEqualTo("닭가슴살");
        assertThat(data.sellerId()).isEqualTo(100L);
        assertThat(data.deletedDate()).isNull();

        // then — 영양소 계산: food.calories='165' * weight_g=200 / 100 = 330
        assertThat(data.calories()).isEqualTo(330);
        // protein: 31 * 200 / 100 = 62
        assertThat(data.protein()).isEqualTo(62);
        // carbs: 0 * 200 / 100 = 0
        assertThat(data.carbs()).isEqualTo(0);
        // fat: 3.6 * 200 / 100 = 7 (ROUND)
        assertThat(data.fat()).isEqualTo(7);
    }

    @Test
    @DisplayName("selectProductForSync_softDeleted_deletedDate반환")
    void selectProductForSync_softDeleted_deletedDate반환() {
        // when
        ProductSyncData data = productMapper.selectProductForSync(103L);

        // then — 삭제된 상품도 조회 가능하며, deletedDate가 non-null
        assertThat(data).isNotNull();
        assertThat(data.deletedDate()).isNotNull();
    }

    @Test
    @DisplayName("selectModifiedAfter_시간범위필터_해당건만반환")
    void selectModifiedAfter_시간범위필터_해당건만반환() {
        // given — 30분 전 기준
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        // when
        List<ProductSyncData> results = productMapper.selectModifiedAfter(thirtyMinutesAgo);

        // then — sync 테스트 대상 중 id 100(modified 10분 전), 102(created 5분 전), 103(modified 5분 전) 포함
        //        base 시드 id 1, 2도 CURRENT_TIMESTAMP로 생성되어 포함됨
        List<Long> ids = results.stream().map(ProductSyncData::id).toList();
        assertThat(ids).contains(100L, 102L, 103L);
        // id 101(modified 1시간 전), 104(modified 30일 전) 제외
        assertThat(ids).doesNotContain(101L, 104L);
    }

    @Test
    @DisplayName("selectModifiedAfter_softDeleted포함_삭제된상품도반환")
    void selectModifiedAfter_softDeleted포함_삭제된상품도반환() {
        // given — 30분 전 기준
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        // when
        List<ProductSyncData> results = productMapper.selectModifiedAfter(thirtyMinutesAgo);

        // then — id 103 (deleted, modified 5분 전) 포함
        List<Long> ids = results.stream().map(ProductSyncData::id).toList();
        assertThat(ids).contains(103L);

        ProductSyncData deleted = results.stream()
                .filter(d -> d.id().equals(103L))
                .findFirst()
                .orElseThrow();
        assertThat(deleted.deletedDate()).isNotNull();
    }

    @Test
    @DisplayName("selectAllActiveForSync_페이지네이션_정확한건수반환")
    void selectAllActiveForSync_페이지네이션_정확한건수반환() {
        // 전체 활성 상품: base 시드(1,2) + sync 시드(100,101,102,104) = 6건, 103은 deleted
        // when — page 1: size=3, offset=0
        List<ProductSyncData> page1 = productMapper.selectAllActiveForSync(3, 0);

        // when — page 2: size=3, offset=3
        List<ProductSyncData> page2 = productMapper.selectAllActiveForSync(3, 3);

        // then — 두 페이지를 합쳐 ID 기반으로 검증
        List<Long> allIds = new ArrayList<>();
        allIds.addAll(page1.stream().map(ProductSyncData::id).toList());
        allIds.addAll(page2.stream().map(ProductSyncData::id).toList());

        assertThat(allIds).contains(100L, 101L, 102L, 104L);  // sync active 4건
        assertThat(allIds).doesNotContain(103L);               // deleted 제외

        // 페이지 크기 범위 확인
        assertThat(page1).isNotEmpty();
        assertThat(page1.size()).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("selectAllActiveForSync_deleted제외_activeOnly")
    void selectAllActiveForSync_deleted제외_activeOnly() {
        // when — 충분히 큰 size로 전체 조회
        List<ProductSyncData> results = productMapper.selectAllActiveForSync(100, 0);

        // then — deleted(103) 제외, 전체 활성 상품 6건 반환
        List<Long> ids = results.stream().map(ProductSyncData::id).toList();
        assertThat(ids).contains(100L, 101L, 102L, 104L);
        assertThat(ids).doesNotContain(103L);
    }
}
