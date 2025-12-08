package com.ssafy.fitmarket_be.address.repository;

import com.ssafy.fitmarket_be.entity.Address;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AddressRepository {

  List<Address> findAllByUserId(@Param("userId") Long userId);

  Optional<Address> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

  int insert(Address address);

  int insertUserAddress(@Param("userId") Long userId, @Param("addressId") Long addressId);

  int update(
      @Param("id") Long id,
      @Param("userId") Long userId,
      @Param("postalCode") String postalCode,
      @Param("addressLine") String addressLine,
      @Param("addressLineDetail") String addressLineDetail
  );

  int delete(@Param("id") Long id, @Param("userId") Long userId);
}
