package com.ssafy.fitmarket_be.address.service;

import com.ssafy.fitmarket_be.address.dto.AddressCreateRequestDto;
import com.ssafy.fitmarket_be.address.dto.AddressResponseDto;
import com.ssafy.fitmarket_be.address.dto.AddressUpdateRequestDto;
import com.ssafy.fitmarket_be.address.mapper.AddressDtoMapper;
import com.ssafy.fitmarket_be.address.repository.AddressRepository;
import com.ssafy.fitmarket_be.entity.Address;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AddressService {

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

  @Transactional
  public Long create(Long userId, AddressCreateRequestDto request) {
    Address address = this.addressDtoMapper.toEntity(request);

    int inserted = this.addressRepository.insert(address);
    if (inserted <= 0) {
      throw new IllegalStateException("배송지를 저장하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }

    int linked = this.addressRepository.insertUserAddress(userId, address.getId());
    if (linked <= 0) {
      throw new IllegalStateException("배송지 연결 과정에서 문제가 발생했어요. 잠시 후 다시 시도해 주세요.");
    }

    return address.getId();
  }

  @Transactional
  public void update(Long userId, Long id, AddressUpdateRequestDto request) {
    validateUpdateRequest(request);

    int updated = this.addressRepository.update(
        id,
        userId,
        request.getPostalCode(),
        request.getAddressLine(),
        request.getAddressLineDetail()
    );

    if (updated <= 0) {
      throw new IllegalArgumentException("수정할 배송지를 찾을 수 없어요. 이미 삭제되었는지 확인해 주세요.");
    }
  }

  @Transactional
  public void delete(Long userId, Long id) {
    int deleted = this.addressRepository.delete(id, userId);
    if (deleted <= 0) {
      throw new IllegalArgumentException("삭제할 배송지를 찾을 수 없어요. 이미 삭제되었는지 확인해 주세요.");
    }
  }

  private void validateUpdateRequest(AddressUpdateRequestDto request) {
    if (!request.hasUpdatableField()) {
      throw new IllegalArgumentException("변경할 주소 정보를 입력해 주세요.");
    }

    if (request.getPostalCode() != null && !StringUtils.hasText(request.getPostalCode())) {
      throw new IllegalArgumentException("우편번호를 다시 확인해 주세요.");
    }

    if (request.getAddressLine() != null && !StringUtils.hasText(request.getAddressLine())) {
      throw new IllegalArgumentException("도로명 주소를 다시 확인해 주세요.");
    }

    if (request.getAddressLineDetail() != null
        && !StringUtils.hasText(request.getAddressLineDetail())) {
      throw new IllegalArgumentException("상세 주소를 다시 확인해 주세요.");
    }
  }
}
