package com.ssafy.fitmarket_be.member.dto;

import java.util.List;

public class MemberListResponse {
    private List<Member> members;
    private int totalCount;
    private int page;
    private int size;

    public MemberListResponse(List<Member> members, int totalCount, int page, int size) {
        this.members = members;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
    }

    public List<Member> getMembers() { return members; }
    public int getTotalCount() { return totalCount; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
