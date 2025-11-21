package com.ssafy.fitmarket_be.member.service;

import com.ssafy.fitmarket_be.dto.Member;
import com.ssafy.fitmarket_be.member.dto.MemberListRequest;
import com.ssafy.fitmarket_be.member.dto.MemberListResponse;
import com.ssafy.fitmarket_be.member.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;

    public MemberListResponse getMembers(MemberListRequest request) {
        List<Member> members = memberMapper.findAll(request.getOffset(), request.getSize());
        int totalCount = memberMapper.countAll();
        return new MemberListResponse(members, totalCount, request.getPage(), request.getSize());
    }
}
