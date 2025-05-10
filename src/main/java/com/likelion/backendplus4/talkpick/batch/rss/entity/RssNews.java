package com.likelion.backendplus4.talkpick.batch.rss.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rss", uniqueConstraints = @UniqueConstraint(columnNames = {"link"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RssNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String link;

    @Column(name = "pub_date")
    private LocalDateTime pubDate;

    @Column
    private String category;

    @Column
    private String guid;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_summary")
    private boolean isSummary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}