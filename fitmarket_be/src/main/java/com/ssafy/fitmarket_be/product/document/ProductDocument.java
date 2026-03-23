package com.ssafy.fitmarket_be.product.document;

import com.ssafy.fitmarket_be.product.domain.Nutrition;
import com.ssafy.fitmarket_be.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/product-settings.json")
@Mapping(mappingPath = "/elasticsearch/product-mappings.json")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String name;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String description;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Float)
    private Float rating;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String categoryName;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String foodName;

    @Field(type = FieldType.Object)
    private NutritionInfo nutrition;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @Field(type = FieldType.Date)
    private LocalDateTime createdDate;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedDate;

    /**
     * Product 도메인 + 연관 데이터를 ES ProductDocument로 변환한다.
     *
     * @param product      MySQL Product 도메인 객체
     * @param foodName     food 테이블의 name (JOIN 결과, nullable)
     * @param sellerId     products 테이블의 user_id (판매자 ID)
     * @param createdDate  products 테이블의 created_date
     * @param updatedDate  products 테이블의 updated_date
     * @return ES 인덱싱용 ProductDocument
     */
    public static ProductDocument from(Product product, String foodName,
                                       Long sellerId, LocalDateTime createdDate,
                                       LocalDateTime updatedDate) {
        Nutrition nutrition = product.getNutrition();

        return ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .rating((float) product.getRating())
                .reviewCount(product.getReviewCount())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategoryId())
                .categoryName(product.getCategoryName())
                .foodName(foodName)
                .nutrition(nutrition != null ? NutritionInfo.builder()
                        .calories((float) nutrition.getCalories())
                        .protein((float) nutrition.getProtein())
                        .carbs((float) nutrition.getCarbs())
                        .fat((float) nutrition.getFat())
                        .build() : null)
                .sellerId(sellerId)
                .createdDate(createdDate)
                .updatedDate(updatedDate)
                .build();
    }
}
