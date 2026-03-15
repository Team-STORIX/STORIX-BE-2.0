package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "works",
        indexes = {
                @Index(name = "idx_works_author", columnList = "author"),
                @Index(name = "idx_works_illustrator", columnList = "illustrator"),
                @Index(name = "idx_works_original_author", columnList = "original_author")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Works {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "works_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId = null;

    // 작품 플랫폼
    @Column(nullable = false)
    private Platform platform;

    // 작품명
    @Column(name = "works_name", nullable = false)
    private String worksName;

    // 전체 작가
    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(length = 100)
    private String author;

    @Column(length = 100)
    private String illustrator;

    @Column(name = "original_author", length = 100)
    private String originalAuthor;

    // 연령 등급
    @Column(name = "age_classification", nullable = false)
    private AgeClassification ageClassification;

    // 작품 소개
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // 작품 장르
    @Column(nullable = false)
    private Genre genre;

    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    @Column(name = "reviews_count")
    private Integer reviewsCount;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "works_type", nullable = false)
    private WorksType worksType;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "works_hashtag",
            joinColumns = @JoinColumn(name = "works_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    @Column(name = "is_onboarding")
    private Boolean isOnboarding;

    @Builder
    private Works(Platform platform, String worksName,
                  String artistName, String author, String illustrator,
                  String originalAuthor, AgeClassification ageClassification,
                  String description, Genre genre, String thumbnailUrl,
                  WorksType worksType) {

        this.platform = platform;
        this.worksName = worksName;
        this.artistName = artistName;
        this.author = author;
        this.illustrator = illustrator;
        this.originalAuthor = originalAuthor;
        this.ageClassification = ageClassification;
        this.description = description;
        this.genre = genre;
        this.thumbnailUrl = thumbnailUrl;
        this.worksType = worksType;
        this.reviewsCount = 0;
        this.avgRating = 0.0;
        this.hashtags = new HashSet<>();
    }

    /**
     * 해시태그 조회 (read-only)
     * */
    public Set<Hashtag> getHashtags() {
        return Set.copyOf(hashtags);
    }
}
