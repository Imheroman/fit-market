package com.ssafy.fitmarket_be.address.mapper;

import com.ssafy.fitmarket_be.address.dto.AddressCreateRequestDto;
import com.ssafy.fitmarket_be.address.dto.AddressResponseDto;
import com.ssafy.fitmarket_be.entity.Address;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressDtoMapper {

  Address toEntity(AddressCreateRequestDto request);

  AddressResponseDto toResponse(Address address);

  List<AddressResponseDto> toResponseList(List<Address> addresses);
}
