package com.ssafy.fitmarket_be.ai.service;

import com.ssafy.fitmarket_be.food.domain.Food;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 기반 상품 매칭 서비스.
 * RAG로 추출된 후보 중에서 가장 유사한 상품을 최종 선택한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * RAG로 추출된 후보 중 가장 유사한 상품을 최종 선택한다.
     *
     * @param productName 상품명
     * @param foods RAG로 추출된 후보 상품 목록
     * @return 매칭된 상품 ID
     */
    public Long findBestMatch(String productName, List<Food> foods) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key가 설정되지 않아 첫 번째 상품을 반환합니다.");
            return foods.isEmpty() ? 1L : foods.get(0).getId();
        }

        if (foods.isEmpty()) {
            log.warn("매칭 가능한 상품이 없습니다. 기본값(1L) 반환.");
            return 1L;
        }

        try {
            log.info("상품명 '{}', 후보 상품 {}개 (RAG 추출)", productName, foods.size());
            String prompt = buildPrompt(productName, foods);
            String response = callOpenAI(prompt);
            Long foodId = parseFoodId(response);

            log.info("매칭 완료 - 상품명 '{}', 매칭된 상품 ID: {}", productName, foodId);
            return foodId;

        } catch (Exception e) {
            log.error("LLM 매칭 실패: {}", e.getMessage(), e);
            // 실패 시 첫 번째 상품 반환 (RAG가 유사 후보를 이미 찾았다고 가정)
            return foods.get(0).getId();
        }
    }

    /**
     * 프롬프트 구성.
     */
    private String buildPrompt(String productName, List<Food> foods) {
        StringBuilder sb = new StringBuilder();
        sb.append("상품명: ").append(productName).append("\n\n");
        sb.append("아래 상품 중 가장 유사한 상품의 ID만 반환하세요.\n");
        sb.append("정확히 일치하지 않아도 괜찮습니다. 칼로리와 영양 성분이 비슷한 것을 선택하세요.\n\n");
        sb.append("상품 목록:\n");

        for (Food food : foods) {
            sb.append(String.format("ID: %d, 이름: %s, 카테고리: %s, 칼로리: %s, 단백질: %sg, 탄수화물: %sg, 지방: %sg\n",
                food.getId(),
                food.getName(),
                food.getFoodCategoryMajor() != null ? food.getFoodCategoryMajor() : "없음",
                food.getCalories() != null ? food.getCalories() : "0",
                food.getProtein() != null ? food.getProtein() : "0",
                food.getCarbs() != null ? food.getCarbs() : "0",
                food.getFat() != null ? food.getFat() : "0"
            ));
        }

        sb.append("\n반드시 JSON 형식으로 반환: {\"food_id\": 숫자}");

        return sb.toString();
    }

    /**
     * OpenAI API 호출.
     */
    private String callOpenAI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 100);

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize chat request", e);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,
            entity,
            Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }

    /**
     * LLM 응답에서 food_id 파싱.
     */
    private Long parseFoodId(String response) {
        // JSON에서 food_id 추출 (간단 파싱)
        // 예: {"food_id": 123}
        String cleaned = response.replaceAll("[^0-9]", "");
        return Long.parseLong(cleaned);
    }
}