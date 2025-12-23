package com.ssafy.fitmarket_be.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SSAFY GMS 커스텀 Embedding 모델.
 * GMS는 input을 배열뿐만 아니라 문자열로도 받기 때문에 Spring AI 기본 구현을 직접 구현한다.
 */
@Slf4j
@Primary
@Component
public class CustomGmsEmbeddingModel extends AbstractEmbeddingModel {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int batchSize;
    private final String apiKey;
    private final String model;

    public CustomGmsEmbeddingModel(
        @Value("${spring.ai.openai.api-key}") String apiKey,
        @Value("${spring.ai.openai.base-url}") String baseUrl,
        @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}") String model,
        @Value("${spring.ai.openai.embedding.batch-size:100}") int batchSize
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.batchSize = batchSize;

        log.info("GMS Embedding 모델 초기화");
        log.info("  Base URL: {}", baseUrl);
        log.info("  Model: {}", model);
        log.info("  API Key: {}***", apiKey.substring(0, Math.min(10, apiKey.length())));

        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.restClient = RestClient.builder()
            .baseUrl(normalizedBaseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> inputs = request.getInstructions();
        if (inputs == null || inputs.isEmpty()) {
            return new EmbeddingResponse(List.of());
        }

        List<Embedding> embeddings = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            embeddings.add(null);
        }

        int offset = 0;
        while (offset < inputs.size()) {
            int end = Math.min(offset + batchSize, inputs.size());
            List<String> batch = inputs.subList(offset, end);

            Object inputPayload = batch.size() == 1 ? batch.get(0) : batch;
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", inputPayload
            );

            String uri = "v1/embeddings";
            log.debug("GMS Embedding 요청: URI={}, Body={}", uri, requestBody);

            String requestJson;
            try {
                requestJson = objectMapper.writeValueAsString(requestBody);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize embedding request", e);
            }

            GmsEmbeddingResponse response = restClient.post()
                .uri(uri)
                .body(requestJson)
                .retrieve()
                .body(GmsEmbeddingResponse.class);

            log.debug("GMS Embedding 응답 수신 완료");

            if (response != null && response.data() != null) {
                for (EmbeddingData data : response.data()) {
                    int index = offset + data.index();
                    float[] embedding = convertToFloatArray(data.embedding());
                    Embedding value = new Embedding(embedding, index);
                    if (index >= 0 && index < embeddings.size()) {
                        embeddings.set(index, value);
                    } else {
                        embeddings.add(value);
                    }
                }
            }

            offset = end;
        }

        embeddings.removeIf(e -> e == null);
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return embed(document.getContent());
    }

    private float[] convertToFloatArray(List<Double> doubles) {
        float[] floats = new float[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) {
            floats[i] = doubles.get(i).floatValue();
        }
        return floats;
    }

    // GMS 응답 DTO
    private record GmsEmbeddingResponse(
        String object,
        List<EmbeddingData> data,
        String model,
        Usage usage
    ) {}

    private record EmbeddingData(
        String object,
        List<Double> embedding,
        int index
    ) {}

    private record Usage(
        int prompt_tokens,
        int total_tokens
    ) {}
}