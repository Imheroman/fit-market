package com.ssafy.fitmarket_be.seller.infrastructure.mybatis;

import com.ssafy.fitmarket_be.seller.domain.Seller;
import com.ssafy.fitmarket_be.seller.domain.SellerStatus;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SellerMapper {

  Optional<Seller> findActiveByUserId(@Param("userId") Long userId);

  Optional<Seller> findActiveById(@Param("id") Long id);

  List<Seller> findByStatus(@Param("status") SellerStatus status);

  int insert(Seller seller);

  int updateForReapply(Seller seller);

  int updateStatus(
      @Param("id") Long id,
      @Param("status") SellerStatus status,
      @Param("reviewNote") String reviewNote,
      @Param("reviewedBy") Long reviewedBy
  );
}
