package com.ssafy.fitmarket_be.seller.application;

import com.ssafy.fitmarket_be.user.entity.User;
import com.ssafy.fitmarket_be.seller.api.dto.SellerCreateRequest;
import com.ssafy.fitmarket_be.seller.api.dto.SellerResponse;
import com.ssafy.fitmarket_be.seller.api.dto.SellerReviewRequest;
import com.ssafy.fitmarket_be.seller.domain.BusinessType;
import com.ssafy.fitmarket_be.seller.domain.Seller;
import com.ssafy.fitmarket_be.seller.domain.SellerStatus;
import com.ssafy.fitmarket_be.seller.infrastructure.mybatis.SellerMapper;
import com.ssafy.fitmarket_be.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SellerServiceImpl")
class SellerServiceImplTest {

    @Mock
    SellerMapper sellerMapper;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    SellerServiceImpl sellerServiceImpl;

    private static final Long USER_ID = 1L;
    private static final Long SELLER_ID = 10L;
    private static final Long REVIEWER_ID = 99L;

    private User mockUser() {
        return User.builder().id(USER_ID).name("홍길동").email("hong@test.com").build();
    }

    private SellerCreateRequest createRequest() {
        return new SellerCreateRequest(
                "테스트 사업체", "123-45-67890", "individual",
                "010-1234-5678", "서울시 강남구", "건강식품 전문 판매업체입니다. 신선하고 건강한 식품을 제공합니다."
        );
    }

    private Seller buildMockSeller(Long id, Long userId, SellerStatus status) {
        Seller seller = mock(Seller.class);
        given(seller.getId()).willReturn(id);
        given(seller.getUserId()).willReturn(userId);
        given(seller.getStatus()).willReturn(status);
        given(seller.isPending()).willReturn(SellerStatus.PENDING.equals(status));
        given(seller.getBusinessName()).willReturn("테스트 사업체");
        given(seller.getBusinessNumber()).willReturn("123-45-67890");
        given(seller.getBusinessType()).willReturn(BusinessType.INDIVIDUAL);
        given(seller.getContactPhone()).willReturn("010-1234-5678");
        given(seller.getBusinessAddress()).willReturn("서울시 강남구");
        given(seller.getIntroduction()).willReturn("소개");
        given(seller.getReviewNote()).willReturn(null);
        given(seller.getReviewedBy()).willReturn(null);
        given(seller.getCreatedDate()).willReturn(null);
        given(seller.getModifiedDate()).willReturn(null);
        return seller;
    }

    @Test
    @DisplayName("신규 신청 시 insert가 호출되고 SellerResponse가 반환된다")
    void apply_신규신청_insert호출() {
        // given
        given(sellerMapper.findActiveByUserId(USER_ID)).willReturn(Optional.empty());
        given(sellerMapper.insert(any())).willReturn(1);
        given(userRepository.findBy(USER_ID)).willReturn(Optional.of(mockUser()));

        // when
        SellerResponse result = sellerServiceImpl.apply(USER_ID, createRequest());

        // then
        verify(sellerMapper).insert(any());
        assertThat(result).isInstanceOf(SellerResponse.class);
    }

    @Test
    @DisplayName("REJECTED 상태의 기존 신청이 있으면 updateForReapply가 호출되고 insert는 호출되지 않는다")
    void apply_REJECTED_재신청_updateForReapply호출() {
        // given
        Seller rejectedSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.REJECTED);
        // reapply는 거절된 Seller에서만 호출 가능
        Seller reapplied = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.PENDING);
        given(rejectedSeller.reapply(anyString(), anyString(), any(), anyString(), anyString(), anyString()))
                .willReturn(reapplied);

        given(sellerMapper.findActiveByUserId(USER_ID)).willReturn(Optional.of(rejectedSeller));
        given(sellerMapper.updateForReapply(any())).willReturn(1);
        given(sellerMapper.findActiveById(SELLER_ID)).willReturn(Optional.of(reapplied));
        given(userRepository.findBy(USER_ID)).willReturn(Optional.of(mockUser()));

        // when
        sellerServiceImpl.apply(USER_ID, createRequest());

        // then
        verify(sellerMapper).updateForReapply(any());
        verify(sellerMapper, never()).insert(any());
    }

    @Test
    @DisplayName("PENDING 상태의 기존 신청이 있으면 중복 신청 IllegalStateException을 던진다")
    void apply_PENDING_중복신청_IllegalStateException() {
        // given
        Seller pendingSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.PENDING);
        given(sellerMapper.findActiveByUserId(USER_ID)).willReturn(Optional.of(pendingSeller));

        // when / then
        assertThatThrownBy(() -> sellerServiceImpl.apply(USER_ID, createRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 접수된 판매자 신청이 있습니다.");
    }

    @Test
    @DisplayName("APPROVED 상태의 기존 신청이 있으면 중복 신청 IllegalStateException을 던진다")
    void apply_APPROVED_중복신청_IllegalStateException() {
        // given
        Seller approvedSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.APPROVED);
        given(sellerMapper.findActiveByUserId(USER_ID)).willReturn(Optional.of(approvedSeller));

        // when / then
        assertThatThrownBy(() -> sellerServiceImpl.apply(USER_ID, createRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 접수된 판매자 신청이 있습니다.");
    }

    @Test
    @DisplayName("APPROVED 결정 시 updateStatus와 updateRole이 호출된다")
    void review_APPROVED_updateRole호출() {
        // given
        Seller pendingSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.PENDING);
        Seller approvedSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.APPROVED);
        given(approvedSeller.getReviewedBy()).willReturn(REVIEWER_ID);
        given(approvedSeller.getReviewNote()).willReturn("승인합니다.");

        // Seller.approve() 호출 → decide() → isPending() 체크 후 새 Seller 반환
        given(pendingSeller.approve(REVIEWER_ID, "승인합니다.")).willReturn(approvedSeller);

        // findActiveById: 처음에는 pendingSeller, refresh 이후엔 approvedSeller
        given(sellerMapper.findActiveById(SELLER_ID))
                .willReturn(Optional.of(pendingSeller))
                .willReturn(Optional.of(approvedSeller));

        given(sellerMapper.updateStatus(eq(SELLER_ID), eq(SellerStatus.APPROVED), any(), eq(REVIEWER_ID)))
                .willReturn(1);
        given(userRepository.updateRole(eq(USER_ID), eq("SELLER"))).willReturn(1);
        given(userRepository.findBy(USER_ID)).willReturn(Optional.of(mockUser()));

        SellerReviewRequest request = new SellerReviewRequest("approved", "승인합니다.");

        // when
        sellerServiceImpl.review(SELLER_ID, REVIEWER_ID, request);

        // then
        verify(sellerMapper).updateStatus(eq(SELLER_ID), eq(SellerStatus.APPROVED), any(), eq(REVIEWER_ID));
        verify(userRepository).updateRole(USER_ID, "SELLER");
    }

    @Test
    @DisplayName("REJECTED 결정 시 updateStatus는 호출되지만 updateRole은 호출되지 않는다")
    void review_REJECTED_updateRole미호출() {
        // given
        Seller pendingSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.PENDING);
        Seller rejectedSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.REJECTED);

        given(rejectedSeller.getReviewedBy()).willReturn(REVIEWER_ID);
        given(rejectedSeller.getReviewNote()).willReturn("거절 사유입니다.");
        given(pendingSeller.reject(REVIEWER_ID, "거절 사유입니다.")).willReturn(rejectedSeller);

        given(sellerMapper.findActiveById(SELLER_ID))
                .willReturn(Optional.of(pendingSeller))
                .willReturn(Optional.of(rejectedSeller));

        given(sellerMapper.updateStatus(eq(SELLER_ID), eq(SellerStatus.REJECTED), any(), eq(REVIEWER_ID)))
                .willReturn(1);
        given(userRepository.findBy(USER_ID)).willReturn(Optional.of(mockUser()));

        SellerReviewRequest request = new SellerReviewRequest("rejected", "거절 사유입니다.");

        // when
        sellerServiceImpl.review(SELLER_ID, REVIEWER_ID, request);

        // then
        verify(sellerMapper).updateStatus(eq(SELLER_ID), eq(SellerStatus.REJECTED), any(), eq(REVIEWER_ID));
        verify(userRepository, never()).updateRole(any(), any());
    }

    @Test
    @DisplayName("PENDING 상태를 결정으로 전달하면 IllegalArgumentException을 던진다")
    void review_PENDING전달_IllegalArgumentException() {
        // given
        Seller pendingSeller = buildMockSeller(SELLER_ID, USER_ID, SellerStatus.PENDING);
        given(sellerMapper.findActiveById(SELLER_ID)).willReturn(Optional.of(pendingSeller));

        SellerReviewRequest request = new SellerReviewRequest("pending", null);

        // when / then
        assertThatThrownBy(() -> sellerServiceImpl.review(SELLER_ID, REVIEWER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("처리 상태는 승인 또는 거절만 가능합니다.");
    }

    @Test
    @DisplayName("판매자 신청이 없으면 getMyApplication은 IllegalArgumentException을 던진다")
    void getMyApplication_신청없음_IllegalArgumentException() {
        // given
        given(sellerMapper.findActiveByUserId(USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sellerServiceImpl.getMyApplication(USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("등록된 판매자 신청을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("status가 null이면 PENDING을 기본값으로 사용하여 조회한다")
    void listByStatus_null_PENDING기본값() {
        // given
        given(sellerMapper.findByStatusWithUser(SellerStatus.PENDING)).willReturn(List.of());

        // when
        sellerServiceImpl.listByStatus(null);

        // then
        verify(sellerMapper).findByStatusWithUser(SellerStatus.PENDING);
    }

    @Test
    @DisplayName("status가 빈 문자열이면 PENDING을 기본값으로 사용하여 조회한다")
    void listByStatus_blank_PENDING기본값() {
        // given
        given(sellerMapper.findByStatusWithUser(SellerStatus.PENDING)).willReturn(List.of());

        // when
        sellerServiceImpl.listByStatus("");

        // then
        verify(sellerMapper).findByStatusWithUser(SellerStatus.PENDING);
    }
}
