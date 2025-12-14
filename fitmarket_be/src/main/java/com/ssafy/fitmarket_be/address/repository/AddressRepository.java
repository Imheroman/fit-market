package com.ssafy.fitmarket_be.address.repository;

import com.ssafy.fitmarket_be.address.dto.AddressUpdateRequestDto;
import com.ssafy.fitmarket_be.entity.Address;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AddressRepository {

  List<Address> findAllByUserId(@Param("userId") Long userId);

  Optional<Address> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

  int save(Address address);

  int insertUserAddress(
      @Param("userId") Long userId,
      @Param("addressId") Long addressId,
      @Param("isMain") boolean isMain
  );

  int update(@Param("userId") Long userId, @Param("id") Long id, @Param("dto")AddressUpdateRequestDto dto);

  int delete(@Param("id") Long id, @Param("userId") Long userId);

  int countActiveByUserId(@Param("userId") Long userId);

  int clearMainByUserId(@Param("userId") Long userId);

  int setMainByUserIdAndAddressId(
      @Param("userId") Long userId,
      @Param("addressId") Long addressId
  );

  Optional<Long> findSingleActiveAddressId(@Param("userId") Long userId);
}
