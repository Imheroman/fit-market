package com.ssafy.fitmarket_be.member.repository;

import com.ssafy.fitmarket_be.dto.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> findAll(@Param("offset") int offset, @Param("size") int size);
    int countAll();
}
