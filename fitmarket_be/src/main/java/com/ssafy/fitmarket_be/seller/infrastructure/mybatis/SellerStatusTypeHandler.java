package com.ssafy.fitmarket_be.seller.infrastructure.mybatis;

import com.ssafy.fitmarket_be.seller.domain.SellerStatus;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(SellerStatus.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class SellerStatusTypeHandler extends BaseTypeHandler<SellerStatus> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, SellerStatus parameter,
      JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter.getCode());
  }

  @Override
  public SellerStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String code = rs.getString(columnName);
    return code == null ? null : SellerStatus.from(code);
  }

  @Override
  public SellerStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String code = rs.getString(columnIndex);
    return code == null ? null : SellerStatus.from(code);
  }

  @Override
  public SellerStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String code = cs.getString(columnIndex);
    return code == null ? null : SellerStatus.from(code);
  }
}
