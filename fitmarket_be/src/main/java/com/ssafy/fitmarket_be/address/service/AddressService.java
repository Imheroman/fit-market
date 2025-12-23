package com.ssafy.fitmarket_be.address.service;

import com.ssafy.fitmarket_be.address.dto.AddressCreateRequestDto;
import com.ssafy.fitmarket_be.address.dto.AddressResponseDto;
import com.ssafy.fitmarket_be.address.dto.AddressUpdateRequestDto;
import com.ssafy.fitmarket_be.address.mapper.AddressDtoMapper;
import com.ssafy.fitmarket_be.address.repository.AddressRepository;
import com.ssafy.fitmarket_be.entity.Address;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddressService {

  private static final int MAX_ADDRESS_COUNT = 5;

  private final AddressRepository addressRepository;
  private final AddressDtoMapper addressDtoMapper;

  @Transactional(readOnly = true)
  public List<AddressResponseDto> findAddresses(Long userId) {
    List<Address> addresses = this.addressRepository.findAllByUserId(userId);
    return this.addressDtoMapper.toResponseList(addresses);
  }

  @Transactional(readOnly = true)
  public AddressResponseDto find(Long userId, Long id) {
    Address address = this.addressRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new IllegalArgumentException("등록된 배송지를 찾을 수 없어요. 다시 확인해 주세요."));

    return this.addressDtoMapper.toResponse(address);
  }

  /**
   * 배송지를 생성하고 사용자와 연결한다.
   *
   * @param userId 배송지를 등록하는 사용자 ID
   * @param request 배송지 생성 요청 DTO
   * @return 생성된 배송지 ID
   * @throws IllegalArgumentException 배송지 등록 가능 수를 초과한 경우
   * @throws IllegalStateException 배송지 저장 또는 연결에 실패한 경우
   */
  @Transactional
  public Long create(Long userId, AddressCreateRequestDto request) {
    int addressCount = this.addressRepository.countActiveByUserId(userId);
    if (addressCount >= MAX_ADDRESS_COUNT) {
      throw new IllegalArgumentException("배송지는 최대 5개까지 등록할 수 있어요. 기존 배송지를 정리한 뒤 다시 시도해 주세요.");
    }

    Address address = this.addressDtoMapper.toEntity(request);
    boolean isMain = Boolean.TRUE.equals(request.getMain());

    int inserted = this.addressRepository.save(address);
    if (inserted <= 0) {
      throw new IllegalStateException("배송지를 저장하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }

    log.trace("inserted address id: {}, user id: {}", address.getId(), userId);

    if (isMain) {
      this.addressRepository.clearMainByUserId(userId);
    }

    int linked = this.addressRepository.insertUserAddress(userId, address.getId(), isMain);
    if (linked <= 0) {
      throw new IllegalStateException("배송지 연결 과정에서 문제가 발생했어요. 잠시 후 다시 시도해 주세요.");
    }

    return address.getId();
  }

  @Transactional
  public void setMain(Long userId, Long addressId) {
    this.addressRepository.findByIdAndUserId(addressId, userId)
        .orElseThrow(() -> new IllegalArgumentException("등록된 배송지를 찾을 수 없어요. 다시 확인해 주세요."));

    this.addressRepository.clearMainByUserId(userId);
    int updated = this.addressRepository.setMainByUserIdAndAddressId(userId, addressId);

    if (updated <= 0) {
      throw new IllegalStateException("기본 배송지로 설정하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }
  }

  @Transactional
  public void update(Long userId, Long id, AddressUpdateRequestDto request) {
    if (request.hasAddressFieldUpdate()) {
      // 업데이트
      int updated = this.addressRepository.update(userId, id, request);

      if (updated <= 0) {
        throw new IllegalArgumentException("수정할 배송지를 찾을 수 없어요. 이미 삭제되었는지 확인해 주세요.");
      }
    } else {
      // 단순 검증용
      this.addressRepository.findByIdAndUserId(id, userId)
          .orElseThrow(() -> new IllegalArgumentException("수정할 배송지를 찾을 수 없어요. 이미 삭제되었는지 확인해 주세요."));
    }

    if (request.shouldUpdateMain()) {
      this.setMain(userId, id);
    }
  }

  @Transactional
  public void delete(Long userId, Long id) {
    int deleted = this.addressRepository.delete(id, userId);
    if (deleted <= 0) {
      throw new IllegalArgumentException("삭제할 배송지를 찾을 수 없어요. 이미 삭제되었는지 확인해 주세요.");
    }

    int remainingAddresses = this.addressRepository.countActiveByUserId(userId);
    if (remainingAddresses == 1) {
      Optional<Long> remainingAddressId = this.addressRepository.findSingleActiveAddressId(userId);
      remainingAddressId.ifPresent(
          addressId -> this.addressRepository.setMainByUserIdAndAddressId(userId, addressId));
    }
  }
}
