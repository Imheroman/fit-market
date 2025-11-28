//package com.ssafy.fitmarket_be.member.controller;
//
//import com.ssafy.fitmarket_be.member.dto.MemberListRequest;
//import com.ssafy.fitmarket_be.member.dto.MemberListResponse;
//import com.ssafy.fitmarket_be.member.service.MemberService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/members")
//public class MemberController {
//
//    private final MemberService memberService;
//
//    public MemberController(MemberService memberService) {
//        this.memberService = memberService;
//    }
//
//    @GetMapping
//    public ResponseEntity<MemberListResponse> getMembers(MemberListRequest request) {
//        MemberListResponse response = memberService.getMembers(request);
//        return ResponseEntity.ok(response);
//    }
//}
