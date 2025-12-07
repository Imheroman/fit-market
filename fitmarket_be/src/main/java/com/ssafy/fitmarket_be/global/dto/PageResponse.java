package com.ssafy.fitmarket_be.global.dto;

import java.util.List;

/**
 * 공통 페이지 응답 DTO.
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {}
