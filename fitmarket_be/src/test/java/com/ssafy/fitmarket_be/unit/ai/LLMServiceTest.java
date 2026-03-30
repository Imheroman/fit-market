package com.ssafy.fitmarket_be.unit.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.fitmarket_be.ai.service.LLMService;
import com.ssafy.fitmarket_be.food.domain.Food;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("LLMService")
class LLMServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    LLMService llmService;

    // ObjectMapper는 실제 인스턴스를 주입한다
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(llmService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(llmService, "apiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(llmService, "model", "gpt-4o-mini");
    }

    // ===== 케이스 1: apiKey 없음 + 목록 있음 → 첫 번째 food id 반환 =====

    @Test
    @DisplayName("findBestMatch: apiKey없음_첫번째상품반환")
    void findBestMatch_apiKey없음_첫번째상품반환() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "");

        Food food1 = mock(Food.class);
        given(food1.getId()).willReturn(10L);
        // food2는 접근되지 않으므로 getId()를 stubbing하지 않는다
        Food food2 = mock(Food.class);

        // when
        Long result = llmService.findBestMatch("닭가슴살", List.of(food1, food2));

        // then
        assertThat(result).isEqualTo(10L);
    }

    // ===== 케이스 2: foods 빈 목록, apiKey 없음 → 1L 반환 =====

    @Test
    @DisplayName("findBestMatch: foods빈목록_1L반환")
    void findBestMatch_foods빈목록_1L반환() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "");

        // when
        Long result = llmService.findBestMatch("닭가슴살", Collections.emptyList());

        // then
        assertThat(result).isEqualTo(1L);
    }

    // ===== 케이스 3: foods 빈 목록, apiKey 있음 → 1L 반환 =====

    @Test
    @DisplayName("findBestMatch: foods빈목록_apiKey있음_1L반환")
    void findBestMatch_foods빈목록_apiKey있음_1L반환() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "valid-key");

        // when
        Long result = llmService.findBestMatch("닭가슴살", Collections.emptyList());

        // then
        assertThat(result).isEqualTo(1L);
    }

    // ===== 케이스 4: parseFoodId 정상 JSON {"food_id": 42} → 42L 반환 (findBestMatch 간접 검증) =====

    @Test
    @DisplayName("parseFoodId: 정상JSON_longId반환")
    void parseFoodId_정상JSON_longId반환() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "valid-key");

        Food food = mock(Food.class);
        given(food.getId()).willReturn(5L);
        given(food.getName()).willReturn("닭가슴살");
        given(food.getFoodCategoryMajor()).willReturn("육류");
        given(food.getCalories()).willReturn("165");
        given(food.getProtein()).willReturn("31");
        given(food.getCarbs()).willReturn("0");
        given(food.getFat()).willReturn("3.6");

        String llmContent = "{\"food_id\": 42}";
        ResponseEntity<Map> responseEntity = buildOpenAiResponse(llmContent);
        given(restTemplate.exchange(any(String.class), any(), any(), eq(Map.class)))
            .willReturn(responseEntity);

        // when
        Long result = llmService.findBestMatch("닭가슴살", List.of(food));

        // then
        assertThat(result).isEqualTo(42L);
    }

    // ===== 케이스 5: parseFoodId food_id 없음 → null → 첫 번째 food 반환 =====

    @Test
    @DisplayName("parseFoodId: food_id없음_null반환")
    void parseFoodId_food_id없음_null반환() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "valid-key");

        Food food = mock(Food.class);
        given(food.getId()).willReturn(7L);
        given(food.getName()).willReturn("현미밥");
        given(food.getFoodCategoryMajor()).willReturn("곡류");
        given(food.getCalories()).willReturn("350");
        given(food.getProtein()).willReturn("7");
        given(food.getCarbs()).willReturn("75");
        given(food.getFat()).willReturn("1");

        // {"result": "없음"} 에는 food_id 키가 없으므로 parseFoodId → null
        String llmContent = "{\"result\": \"없음\"}";
        ResponseEntity<Map> responseEntity = buildOpenAiResponse(llmContent);
        given(restTemplate.exchange(any(String.class), any(), any(), eq(Map.class)))
            .willReturn(responseEntity);

        // when — parseFoodId returns null → fallback to first food
        Long result = llmService.findBestMatch("현미밥", List.of(food));

        // then
        assertThat(result).isEqualTo(7L);
    }

    // ===== 케이스 6: parseFoodId food_id null 값 → null → 첫 번째 food 반환 =====

    @Test
    @DisplayName("parseFoodId: null값_null반환")
    void parseFoodId_null값_null반환() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "valid-key");

        Food food = mock(Food.class);
        given(food.getId()).willReturn(9L);
        given(food.getName()).willReturn("두부");
        given(food.getFoodCategoryMajor()).willReturn("두류");
        given(food.getCalories()).willReturn("80");
        given(food.getProtein()).willReturn("8");
        given(food.getCarbs()).willReturn("2");
        given(food.getFat()).willReturn("4");

        // {"food_id": null} 에서 isNull() == true → parseFoodId → null
        String llmContent = "{\"food_id\": null}";
        ResponseEntity<Map> responseEntity = buildOpenAiResponse(llmContent);
        given(restTemplate.exchange(any(String.class), any(), any(), eq(Map.class)))
            .willReturn(responseEntity);

        // when — parseFoodId returns null → fallback to first food
        Long result = llmService.findBestMatch("두부", List.of(food));

        // then
        assertThat(result).isEqualTo(9L);
    }

    // ===== 케이스 7: LLM 호출 시 RuntimeException → 첫 번째 food fallback =====

    @Test
    @DisplayName("findBestMatch: LLM예외_첫번째상품폴백")
    void findBestMatch_LLM예외_첫번째상품폴백() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "valid-key");

        Food food = mock(Food.class);
        given(food.getId()).willReturn(3L);
        given(food.getName()).willReturn("연어");
        given(food.getFoodCategoryMajor()).willReturn("어류");
        given(food.getCalories()).willReturn("200");
        given(food.getProtein()).willReturn("20");
        given(food.getCarbs()).willReturn("0");
        given(food.getFat()).willReturn("12");

        given(restTemplate.exchange(any(String.class), any(), any(), eq(Map.class)))
            .willThrow(new RuntimeException("OpenAI API timeout"));

        // when
        Long result = llmService.findBestMatch("연어", List.of(food));

        // then
        assertThat(result).isEqualTo(3L);
    }

    // ===== 케이스 8: LLM 응답 food_id 없음 → parseFoodId null → 첫 번째 food fallback =====

    @Test
    @DisplayName("findBestMatch: LLM응답food_id없음_첫번째상품폴백")
    void findBestMatch_LLM응답food_id없음_첫번째상품폴백() {
        // given
        ReflectionTestUtils.setField(llmService, "apiKey", "valid-key");

        Food food = mock(Food.class);
        given(food.getId()).willReturn(15L);
        given(food.getName()).willReturn("고구마");
        given(food.getFoodCategoryMajor()).willReturn("채소류");
        given(food.getCalories()).willReturn("130");
        given(food.getProtein()).willReturn("2");
        given(food.getCarbs()).willReturn("30");
        given(food.getFat()).willReturn("0");

        // 유효하지 않은 JSON으로 parseFoodId가 null 반환하도록 유도
        String llmContent = "잘못된 응답 형식";
        ResponseEntity<Map> responseEntity = buildOpenAiResponse(llmContent);
        given(restTemplate.exchange(any(String.class), any(), any(), eq(Map.class)))
            .willReturn(responseEntity);

        // when — parseFoodId returns null → fallback to first food
        Long result = llmService.findBestMatch("고구마", List.of(food));

        // then
        assertThat(result).isEqualTo(15L);
    }

    // ===== Helper: OpenAI 응답 형식 빌더 =====

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map> buildOpenAiResponse(String content) {
        Map<String, Object> message = Map.of("role", "assistant", "content", content);
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> body = Map.of("choices", List.of(choice));
        return ResponseEntity.ok(body);
    }
}
