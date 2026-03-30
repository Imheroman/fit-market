package com.ssafy.fitmarket_be.ranking.controller;

import com.ssafy.fitmarket_be.ranking.dto.RankingResponse;
import com.ssafy.fitmarket_be.ranking.service.ProductRankingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {
    private final ProductRankingService rankingService;

    @GetMapping("/products")
    public ResponseEntity<List<RankingResponse>> getTopProducts(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rankingService.getTopProducts(period, limit));
    }
}
