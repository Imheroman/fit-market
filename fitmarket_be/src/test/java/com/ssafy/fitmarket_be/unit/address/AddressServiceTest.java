package com.ssafy.fitmarket_be.unit.address;

import com.ssafy.fitmarket_be.address.dto.AddressCreateRequestDto;
import com.ssafy.fitmarket_be.address.dto.AddressUpdateRequestDto;
import com.ssafy.fitmarket_be.address.mapper.AddressDtoMapper;
import com.ssafy.fitmarket_be.address.repository.AddressRepository;
import com.ssafy.fitmarket_be.address.service.AddressService;
import com.ssafy.fitmarket_be.address.entity.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService")
class AddressServiceTest {

    @Mock
    AddressRepository addressRepository;

    @Mock
    AddressDtoMapper addressDtoMapper;

    @InjectMocks
    AddressService addressService;

    // ===== create() =====

    @Test
    @DisplayName("create: 정상 생성 시 addressId를 반환한다")
    void create_정상_addressId반환() {
        // given
        Long userId = 1L;
        AddressCreateRequestDto request = buildCreateRequest(false);
        Address address = new Address();

        given(addressRepository.countActiveByUserId(userId)).willReturn(4);
        given(addressDtoMapper.toEntity(request)).willReturn(address);
        doAnswer(invocation -> {
            Address arg = invocation.getArgument(0);
            arg.setId(10L);
            return 1;
        }).when(addressRepository).save(any(Address.class));
        given(addressRepository.insertUserAddress(userId, 10L, false)).willReturn(1);

        // when
        Long result = addressService.create(userId, request);

        // then
        assertThat(result).isEqualTo(10L);
    }

    @Test
    @DisplayName("create: 배송지 최대 개수(5개) 초과 시 IllegalArgumentException을 던진다")
    void create_최대초과_IllegalArgumentException() {
        // given
        Long userId = 1L;
        AddressCreateRequestDto request = buildCreateRequest(false);
        given(addressRepository.countActiveByUserId(userId)).willReturn(5);

        // when & then
        assertThatThrownBy(() -> addressService.create(userId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("배송지는 최대 5개까지 등록할 수 있어요.");
    }

    @Test
    @DisplayName("create: isMain=true이면 clearMainByUserId가 insertUserAddress보다 먼저 호출된다")
    void create_isMain_true_clearMain먼저호출() {
        // given
        Long userId = 1L;
        AddressCreateRequestDto request = buildCreateRequest(true);
        Address address = new Address();

        given(addressRepository.countActiveByUserId(userId)).willReturn(0);
        given(addressDtoMapper.toEntity(request)).willReturn(address);
        doAnswer(invocation -> {
            Address arg = invocation.getArgument(0);
            arg.setId(5L);
            return 1;
        }).when(addressRepository).save(any(Address.class));
        given(addressRepository.clearMainByUserId(userId)).willReturn(1);
        given(addressRepository.insertUserAddress(userId, 5L, true)).willReturn(1);

        // when
        addressService.create(userId, request);

        // then
        InOrder order = inOrder(addressRepository);
        order.verify(addressRepository).clearMainByUserId(userId);
        order.verify(addressRepository).insertUserAddress(userId, 5L, true);
    }

    @Test
    @DisplayName("create: isMain=false이면 clearMainByUserId가 호출되지 않는다")
    void create_isMain_false_clearMain미호출() {
        // given
        Long userId = 1L;
        AddressCreateRequestDto request = buildCreateRequest(false);
        Address address = new Address();

        given(addressRepository.countActiveByUserId(userId)).willReturn(0);
        given(addressDtoMapper.toEntity(request)).willReturn(address);
        doAnswer(invocation -> {
            Address arg = invocation.getArgument(0);
            arg.setId(5L);
            return 1;
        }).when(addressRepository).save(any(Address.class));
        given(addressRepository.insertUserAddress(userId, 5L, false)).willReturn(1);

        // when
        addressService.create(userId, request);

        // then
        verify(addressRepository, never()).clearMainByUserId(any());
    }

    // ===== delete() =====

    @Test
    @DisplayName("delete: 메인 배송지 삭제 후 잔여 배송지가 있으면 첫 번째 배송지를 메인으로 설정한다")
    void delete_메인배송지삭제후_잔여있음_firstActive설정() {
        // given
        Long userId = 1L;
        Long addressId = 1L;
        Address target = Address.builder().id(addressId).main(true).build();

        given(addressRepository.findByIdAndUserId(addressId, userId)).willReturn(Optional.of(target));
        given(addressRepository.delete(addressId, userId)).willReturn(1);
        given(addressRepository.findFirstActiveAddressId(userId)).willReturn(Optional.of(2L));
        given(addressRepository.setMainByUserIdAndAddressId(userId, 2L)).willReturn(1);

        // when
        addressService.delete(userId, addressId);

        // then
        verify(addressRepository).setMainByUserIdAndAddressId(userId, 2L);
    }

    @Test
    @DisplayName("delete: 비메인 배송지 삭제 시 자동 메인 설정이 없다")
    void delete_비메인배송지삭제_자동설정없음() {
        // given
        Long userId = 1L;
        Long addressId = 2L;
        Address target = Address.builder().id(addressId).main(false).build();

        given(addressRepository.findByIdAndUserId(addressId, userId)).willReturn(Optional.of(target));
        given(addressRepository.delete(addressId, userId)).willReturn(1);

        // when
        addressService.delete(userId, addressId);

        // then
        verify(addressRepository, never()).setMainByUserIdAndAddressId(any(), any());
    }

    @Test
    @DisplayName("delete: 메인 배송지 삭제 후 잔여 배송지가 없으면 자동 설정이 없다")
    void delete_메인배송지삭제후_잔여없음_자동설정없음() {
        // given
        Long userId = 1L;
        Long addressId = 1L;
        Address target = Address.builder().id(addressId).main(true).build();

        given(addressRepository.findByIdAndUserId(addressId, userId)).willReturn(Optional.of(target));
        given(addressRepository.delete(addressId, userId)).willReturn(1);
        given(addressRepository.findFirstActiveAddressId(userId)).willReturn(Optional.empty());

        // when
        addressService.delete(userId, addressId);

        // then
        verify(addressRepository, never()).setMainByUserIdAndAddressId(any(), any());
    }

    // ===== setMain() =====

    @Test
    @DisplayName("setMain: 존재하지 않는 배송지이면 IllegalArgumentException을 던진다")
    void setMain_존재하지않는배송지_IllegalArgumentException() {
        // given
        given(addressRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addressService.setMain(1L, 999L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== update() =====

    @Test
    @DisplayName("update: 주소 필드가 있으면 update가 호출된다")
    void update_주소필드있음_update호출() {
        // given
        Long userId = 1L;
        Long addressId = 1L;
        AddressUpdateRequestDto request = new AddressUpdateRequestDto();
        request.setAddressLine("서울시 강남구");  // has address field
        request.setMain(false);  // NPE 방지: shouldUpdateMain() 호출 시 null unboxing 방지
        given(addressRepository.update(userId, addressId, request)).willReturn(1);

        // when
        addressService.update(userId, addressId, request);

        // then
        verify(addressRepository).update(any(), any(), any());
    }

    @Test
    @DisplayName("update: 주소 필드가 없으면 update가 호출되지 않는다")
    void update_주소필드없음_update미호출() {
        // given
        Long userId = 1L;
        Long addressId = 1L;
        AddressUpdateRequestDto request = new AddressUpdateRequestDto();
        // no address field set — hasAddressFieldUpdate() returns false
        // main is null — shouldUpdateMain() will fail if called, but we handle that below
        // We need main to be non-null false to avoid NPE in shouldUpdateMain()
        request.setMain(false);
        given(addressRepository.findByIdAndUserId(addressId, userId))
            .willReturn(Optional.of(Address.builder().id(addressId).build()));

        // when
        addressService.update(userId, addressId, request);

        // then
        verify(addressRepository, never()).update(anyLong(), anyLong(), any());
    }

    // ===== 헬퍼 메서드 =====

    private AddressCreateRequestDto buildCreateRequest(boolean main) {
        AddressCreateRequestDto request = new AddressCreateRequestDto();
        request.setName("집");
        request.setRecipient("홍길동");
        request.setPhone("01012345678");
        request.setPostalCode("12345");
        request.setAddressLine("서울시 강남구");
        request.setAddressLineDetail("101호");
        request.setMain(main);
        return request;
    }
}
