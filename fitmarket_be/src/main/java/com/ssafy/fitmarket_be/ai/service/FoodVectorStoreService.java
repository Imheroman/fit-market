package com.ssafy.fitmarket_be.ai.service;

import com.ssafy.fitmarket_be.food.domain.Food;
import com.ssafy.fitmarket_be.food.repository.FoodMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG(Retrieval-Augmented Generation) 기반 상품 검색 서비스.
 *
 * <p>도메인 모델 패턴 준수:
 * <ul>
 *   <li>Food는 불변(immutable) 도메인 객체 - final 필드, getter만 존재</li>
 *   <li>Food 객체 생성은 정적 팩토리로만 수행하고 변경하지 않음</li>
 *   <li>모든 변환/매핑은 순수 함수로 구현 (부수효과 없음)</li>
 *   <li>캐시는 불변 객체맵으로 유지 (Collectors.toUnmodifiableMap)</li>
 * </ul>
 *
 * <p>동작 방식:
 * <ol>
 *   <li>서버 시작 시 전체 Food 불변 객체 로드 후 벡터 스토어에 적재</li>
 *   <li>상품 등록 시 상품명 벡터 검색으로 상위 후보 추출</li>
 *   <li>LLM 최종 매칭: 후보만 전달하여 비용/시간 절감</li>
 * </ol>
 *
 * @see Food 불변 도메인 모델
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodVectorStoreService {

    private final FoodMapper foodMapper;
    private final EmbeddingModel embeddingModel;

    // 불변 Food 도메인 객체 캐시 (초기화 단계에서 사용)
    private VectorStore vectorStore;
    private Map<Long, Food> foodCache;  // toUnmodifiableMap()으로 초기화

    /**
     * 서버 시작 시 모든 Food 데이터를 임베딩하여 벡터 스토어에 적재.
     * Food는 불변 객체로 한 번 로드되면 변경되지 않는다.
     */
    @PostConstruct
    public void init() {
        log.info("Food 벡터 스토어 초기화 시작...");

        try {
            initializeVectorStore();
            log.info("Food 벡터 스토어 초기화 완료");
        } catch (Exception e) {
            log.error("Food 벡터 스토어 초기화 실패", e);
            initializeEmpty();
        }
    }

    /**
     * 벡터 스토어 초기화 (불변 Food 도메인 모델 로드).
     */
    private void initializeVectorStore() {
        // 1. VectorStore 생성
        this.vectorStore = new SimpleVectorStore(embeddingModel);

        // 2. 모든 Food 불변 객체 로드 (DB에서 1회 조회)
        List<Food> allFoods = foodMapper.selectAllFoods(50);
        log.info("Food 데이터 로딩 완료: {}건", allFoods.size());

        // 3. Food ID -> Food 불변 객체 캐시 생성
        this.foodCache = allFoods.stream()
            .collect(Collectors.toUnmodifiableMap(
                Food::getId,
                food -> food  // 불변 객체 보관
            ));

        // 4. Food -> Document 변환 (순수 함수 방식)
        List<Document> documents = allFoods.stream()
            .map(this::toDocument)
            .toList();

        // 5. 벡터 스토어에 임베딩 적재
        log.info("임베딩 시작... ({}건, 시간이 걸릴 수 있습니다)", documents.size());
        vectorStore.add(documents);
        log.info("임베딩 완료: {}건 문서", documents.size());
    }

    /**
     * 초기화 실패 시 빈 상태로 설정.
     */
    private void initializeEmpty() {
        this.vectorStore = new SimpleVectorStore(embeddingModel);
        this.foodCache = Map.of();  // 불변 빈 맵
    }

    /**
     * 상품명과 유사한 Food를 검색한다.
     * Food는 불변 객체이므로 캐시에서 직접 조회한다.
     *
     * @param productName 상품명
     * @param topK 반환할 유사 Food 개수
     * @return 유사 Food 목록 (불변 객체 리스트)
     */
    public List<Food> searchSimilarFoods(String productName, int topK) {
        if (vectorStore == null || foodCache == null) {
            log.warn("벡터 스토어가 초기화되지 않았습니다.");
            return List.of();  // 불변 빈 리스트 반환
        }

        try {
            // 벡터 유사도 검색
            List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(productName).withTopK(topK)
            );

            // Document -> Food 변환 (순수 함수, 불변 객체 유지)
            List<Food> similarFoods = similarDocs.stream()
                .map(this::extractFoodFromDocument)
                .filter(food -> food != null)  // null 제거
                .toList();  // 불변 리스트 반환

            log.info("상품명 '{}' 검색 결과: {}개 후보 발견", productName, similarFoods.size());
            return similarFoods;

        } catch (Exception e) {
            log.error("유사 상품 검색 실패: {}", e.getMessage(), e);
            return List.of();  // 불변 빈 리스트 반환
        }
    }

    /**
     * Document에서 Food ID를 추출하여 캐시에서 불변 Food 객체 조회.
     */
    private Food extractFoodFromDocument(Document doc) {
        try {
            Long foodId = ((Number) doc.getMetadata().get("food_id")).longValue();
            return foodCache.get(foodId);  // 불변 객체 반환
        } catch (Exception e) {
            log.warn("Document에서 Food 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 불변 Food 도메인 객체를 Document로 변환.
     * Food 객체는 생성 후 변경되지 않는다.
     */
    private Document toDocument(Food food) {
        // Food 불변 객체에서 내용 생성 (getter만 사용)
        String content = createFoodDescription(food);
        Map<String, Object> metadata = createMetadata(food);
        String documentId = String.valueOf(food.getId());

        // Document는 불변 방식으로 생성 (생성자에 모든 값 전달)
        return new Document(documentId, content, metadata);
    }

    /**
     * Food 메타데이터 생성 (불변 Map).
     */
    private Map<String, Object> createMetadata(Food food) {
        return Map.of(
            "food_id", food.getId(),
            "name", food.getName(),
            "category", food.getFoodCategoryMajor() != null ? food.getFoodCategoryMajor() : ""
        );
    }

    /**
     * Food 객체를 임베딩용 텍스트로 변환.
     * 이름, 카테고리, 영양 정보를 포함해 검색이 용이하도록 구성한다.
     */
    private String createFoodDescription(Food food) {
        return String.format(
            "이름: %s, 대분류: %s, 소분류: %s, 칼로리: %skcal, 단백질: %sg, 탄수화물: %sg, 지방: %sg",
            food.getName(),
            food.getFoodCategoryMajor() != null ? food.getFoodCategoryMajor() : "없음",
            food.getFoodCategoryMinor() != null ? food.getFoodCategoryMinor() : "없음",
            food.getCalories() != null ? food.getCalories() : "0",
            food.getProtein() != null ? food.getProtein() : "0",
            food.getCarbs() != null ? food.getCarbs() : "0",
            food.getFat() != null ? food.getFat() : "0"
        );
    }
}