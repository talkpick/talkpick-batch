package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


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
    private String title;

    @Column(nullable = false, unique = true)
    private String link;

    @Setter
    @Column(name = "pub_date")
    private LocalDateTime pubDate;

    @Column
    private String category;

    @Column
    private String guid;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Setter
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getDescription(){
        return description != null ? description : "";
    }

    public String getSummary() {
        return summary != null ? summary : "";
    }

}