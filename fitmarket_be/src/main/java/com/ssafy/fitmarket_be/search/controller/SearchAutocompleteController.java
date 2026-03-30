package com.ssafy.fitmarket_be.search.controller;

import com.ssafy.fitmarket_be.global.common.ApiResponse;
import com.ssafy.fitmarket_be.search.dto.AutocompleteResponse;
import com.ssafy.fitmarket_be.search.dto.ProductSuggestionResponse;
import com.ssafy.fitmarket_be.search.service.SearchAutocompleteService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Validated
public class SearchAutocompleteController {

    private final SearchAutocompleteService autocompleteService;

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<AutocompleteResponse>> autocomplete(
            @RequestParam @NotBlank @Size(min = 1, max = 100) String q,
            @RequestParam(defaultValue = "8") @Min(1) @Max(50) int limit) {

        List<ProductSuggestionResponse> products = autocompleteService.getSuggestions(q, limit);

        return ResponseEntity.ok(ApiResponse.success(new AutocompleteResponse(products)));
    }
}
