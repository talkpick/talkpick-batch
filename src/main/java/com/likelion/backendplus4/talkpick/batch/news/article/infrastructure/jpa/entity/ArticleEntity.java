package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity;

import java.time.LocalDateTime;

import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.converter.FloatArrayToJsonConverter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * RSS 피드를 수집 객체
 *
 * @author 양병학
 * @since 2025-05-10 최초 작성
 * @modify 2025-05-10 17:47 PR 수정
 * @ToString exclude로 대량의 텍스트필드 로그에서 제외
 * @Data -> @Getter후 Setter는 개별 지정해서 식별자 보호
 * @EqualsAndHashCode 지정으로 갹채 비교 최적화
 */
@Entity
@Table(name = "article", uniqueConstraints = @UniqueConstraint(columnNames = {"link"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "description")
@EqualsAndHashCode(of = "id")
public class ArticleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Setter
    @Column(nullable = false)
    @NotBlank(message = "제목은 필수 값입니다")
    @Size(max = 500, message = "제목은 최대 500자까지 허용됩니다")
    private String title;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "링크는 필수 값입니다")
    @URL(message = "유효한 URL 형식이어야 합니다")
    @Size(max = 255, message = "링크는 최대 255자까지 허용됩니다")
    private String link;

    @Setter
    @Column(name = "pub_date")
    @NotNull(message = "발행일은 필수 값입니다")
    @PastOrPresent(message = "발행일은 현재 또는 과거 날짜여야 합니다")
    private LocalDateTime pubDate;

    @Column
    @NotBlank(message = "카테고리는 필수 값입니다")
    @Size(max =10, message = "카테고리는 최대 10자까지 허용됩니다")
    private String category;

    @Column
    @NotBlank(message = "GUID는 필수 값입니다")
    @Size(max = 255, message = "GUID는 최대 255자까지 허용됩니다")
    @Pattern(regexp = "^[A-Z]{2}\\d+$", message = "GUID는 2개의 대문자와 숫자로 구성되어야 합니다") // 예: KM12345
    private String guid;

    @Setter
      @Column(columnDefinition = "TEXT")
      private String description;

    @Setter
    @Column(name = "image_url")
    @Size(max = 1000, message = "이미지 URL은 최대 1000자까지 허용됩니다")
    private String imageUrl;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

    @Setter
    @Column(name = "summary", columnDefinition = "TEXT")
    @Size(max = 1000, message = "요약은 최대 1000자까지 허용됩니다")
    private String summary;

	@Convert(converter = FloatArrayToJsonConverter.class)
	@Column(name = "summary_vector", columnDefinition = "JSON")
	private float[] summaryVector;

	public ArticleEntity changeSummaryVector(float[] vector) {
		summaryVector = vector;
		return this;
	}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public String getDescription() {
		return description != null ? description : "";
	}
}